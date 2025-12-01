package com.juandroiddev.chirp.api.websocket

import com.fasterxml.jackson.databind.JsonMappingException
import com.juandroiddev.chirp.api.dto.ws.*
import com.juandroiddev.chirp.api.mappers.toChatMessageDto
import com.juandroiddev.chirp.domain.event.ChatParticipantJoinedEvent
import com.juandroiddev.chirp.domain.event.ChatParticipantLeftEvent
import com.juandroiddev.chirp.domain.event.MessageDeletedEvent
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.service.ChatMessageService
import com.juandroiddev.chirp.service.ChatService
import com.juandroiddev.chirp.service.JWTService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jwtService: JWTService
) : TextWebSocketHandler() {

    companion object{
        private const val PING_INTERVAL_MS = 30_000L
        private const val PONG_INTERVAL_MS = 60_000L
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock() // Multiple readers, single writer, no reader allowed when writing

    // sessionId -> UserSession (the actual WebSocket connection and user info)
    private val sessions = ConcurrentHashMap<String, UserSession>()

    // userId -> sessionIds (allows finding all active sessions for a user - useful when user has multiple devices)
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()

    // userId -> chatIds (caches which chats a user belongs to - avoids repeated DB queries)
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()

    // chatId -> sessionIds (allows broadcasting messages to all participants in a chat)
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        // Handle new connection
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing Authorization header.")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                return
            }

        val userId = jwtService.getUserIdFromToken(authHeader)

        val userSession = UserSession(
            userId = userId,
            session = session
        )

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId){ _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val chatIds = userChatIds.computeIfAbsent(userId){
                val chatIds = chatService.findChatsByUserId(userId)
                    .map { it.id }

                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(chatIds)
                }
            }

            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId){ _, existingSessions ->
                    (existingSessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        logger.info("WebSocket connection established for user $userId with session ${session.id}")

    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        connectionLock.write {
            sessions.remove(session.id)?.let { userSession ->
                val userId = userSession.userId

                userToSessions.compute(userId){_, sessions ->
                    sessions
                        ?.apply { remove(session.id) }
                        ?.takeIf { it.isNotEmpty() }
                }

                userChatIds[userId]?.forEach { chatId ->
                    chatToSessions.compute(chatId){ _, sessions ->
                        sessions
                            ?.apply { remove(session.id) }
                            ?.takeIf { it.isNotEmpty() }
                    }
                }
                logger.info("WebSocket connection closed for user $userId")
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error on session ${session.id}", exception)
        session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"))
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message on session ${session.id}: ${message.payload}")

        val userSession = connectionLock.read {
            sessions[session.id]
        } ?: return

        try {
            val webSocketMessage = objectMapper.readValue(
                message.payload,
                IncomingWebSocketMessage::class.java
            )

            when(webSocketMessage.type){
                IncomingWebSocketEventType.NEW_MESSAGE -> {
                    val dto = objectMapper.readValue(
                        webSocketMessage.payload,
                        SendMessageDto::class.java
                    )
                    handleSendMessage(
                        dto = dto,
                        senderId = userSession.userId,
                    )
                }
            }
        }
        catch (e: JsonMappingException){
            logger.error("Couldn't map incoming WebSocket message to DTO ${message.payload}",e)
            sendError(
                session = session,
                errorMessage = ErrorDto(
                    code = "INVALID_JSON",
                    message = "Incoming  JSON or UUID is invalid"
                )
            )
        }
    }

    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
       connectionLock.write {
              sessions.compute(session.id){ _, userSession ->
                  userSession?.copy(
                      lastPongTimeStamp = System.currentTimeMillis()
                  )
              }
       }
        logger.debug("Received pong from session ${session.id}")
    }


    @Scheduled(fixedDelay =  PING_INTERVAL_MS)
    fun pingClients(){

        val currentTime= System.currentTimeMillis()
        val sessionToClose = mutableListOf<String>()

        val sessionsSnapshot = connectionLock.read { sessions.toMap() }

        sessionsSnapshot.forEach { (sessionId, userSession) ->

            try {
                if(userSession.session.isOpen){
                    val lastPong = userSession.lastPongTimeStamp
                    if (currentTime - lastPong > PONG_INTERVAL_MS){
                        logger.warn("Session $sessionId has timed out, closing connection.")
                        sessionToClose.add(sessionId)
                        return@forEach
                    }

                    userSession.session.sendMessage(PingMessage())
                    logger.debug("Sent ping to session {}", userSession.userId)
                }

            }
            catch (e: Exception){
                logger.error("Could not ping session $sessionId",e)
                sessionToClose.add(sessionId)
            }

            sessionToClose.forEach { sessionId ->
                connectionLock.read {
                    sessions[sessionId]?.session?.let { session->
                        try{
                            session.close(CloseStatus.GOING_AWAY.withReason("Ping timeout"))
                        }
                        catch (e: Exception){

                        }
                    }
                }
            }

        }

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(event: MessageDeletedEvent){
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketEventType.MESSAGE_DELETED,
                payload = objectMapper.writeValueAsString(
                    DeleteMessageDto(
                        messageId = event.messageId,
                        chatId = event.chatId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(event: ChatParticipantJoinedEvent) {
        connectionLock.write {
            event.userId.forEach { userId ->
                userChatIds.compute(userId) { _, chatIds ->
                    (chatIds ?: mutableSetOf()).apply {
                        add(event.chatId)
                    }
                }
                userToSessions[userId]?.forEach { sessionId->
                    chatToSessions.compute(event.chatId) { _, sessionIds ->
                        (sessionIds ?: mutableSetOf()).apply {
                            add(sessionId)
                        }
                    }
                }
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketEventType.CHAT_PARTICIPANT_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantChangedDto(
                        chatId = event.chatId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeftChat(event: ChatParticipantLeftEvent) {
        connectionLock.write {
            userChatIds.compute(event.userId){ _, chatIds ->
                chatIds
                    ?.apply { remove(event.chatId) }
                    ?.takeIf { it.isNotEmpty() }

            }
            userToSessions[event.userId]?.forEach { sessionId->
                chatToSessions.compute(event.chatId) { _, sessions ->
                    sessions
                        ?.apply { remove(sessionId) }
                        ?.takeIf { it.isNotEmpty() }
                }
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketEventType.CHAT_PARTICIPANT_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantChangedDto(
                        chatId = event.chatId
                    )
                )
            )
        )
    }

    private fun sendError(
        session: WebSocketSession,
        errorMessage: ErrorDto
    ){
        val webSocketMessage = objectMapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketEventType.ERROR,
                payload = objectMapper.writeValueAsString(errorMessage)
            )
        )
        try {
            session.sendMessage(TextMessage(webSocketMessage))
        }
        catch (e: Exception){
            logger.error("Couldn't send error messages",e)
        }
    }

    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage
    ){
        val chatSessions = connectionLock.read {
            chatToSessions[chatId]?.toList()?: emptyList()
        }

        chatSessions.forEach { sessionId ->

            val userSession = connectionLock.read {
                sessions[sessionId]
            }?: return@forEach

            sendToUser(
                userId = userSession.userId,
                message = message
            )

        }
    }

    private fun handleSendMessage(
        dto: SendMessageDto,
        senderId: UserId,
    ){
        val userChats = connectionLock.read {
            userChatIds[senderId]
        }?: return

        if (dto.chatId !in userChats){
            logger.warn("User $senderId attempted to send message to chat ${dto.chatId} they do not belong to.")
            return
        }
        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )

        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketEventType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(
                    savedMessage.toChatMessageDto()
                )
            )
        )

    }

    private fun sendToUser(userId: UserId,message: OutgoingWebSocketMessage){
        val userSessions = connectionLock.read {
            userToSessions[userId]?: emptySet()
        }

        userSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId] ?: return@forEach
            }
            if (userSession.session.isOpen){
                try{
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.info("Sent message to user $userId on session $sessionId: $message")
                }catch (e: Exception){
                    logger.error("Error while sending message to user $userId on session $sessionId",e)
                }
            }


        }

    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
        val lastPongTimeStamp: Long = System.currentTimeMillis()
    )
}