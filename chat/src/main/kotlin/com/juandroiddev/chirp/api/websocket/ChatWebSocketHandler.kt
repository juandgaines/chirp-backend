package com.juandroiddev.chirp.api.websocket

import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.service.ChatMessageService
import com.juandroiddev.chirp.service.ChatService
import com.juandroiddev.chirp.service.JWTService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jwtService: JWTService
) : TextWebSocketHandler() {


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

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession
    )
}