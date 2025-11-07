package com.juandroiddev.chirp.infra.messaging

import com.juandroiddev.chirp.domain.events.user.UserEvent
import com.juandroiddev.chirp.domain.models.ChatParticipant
import com.juandroiddev.chirp.infra.message_queue.MessageQueues
import com.juandroiddev.chirp.service.ChatParticipantService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService
) {
    private val logger = LoggerFactory.getLogger(ChatUserEventListener::class.java)

    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserCreatedEvent(event: UserEvent) {
        logger.info("Received user event: $event")
        when (event){
            is UserEvent.Verified -> {
                chatParticipantService.createChatParticipant(
                    chatParticipant = ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profilePictureUrl = null
                    )
                )
            }
            else -> Unit
        }
    }
}