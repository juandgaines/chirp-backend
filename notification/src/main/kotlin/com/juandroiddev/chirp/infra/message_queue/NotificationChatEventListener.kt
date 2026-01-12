package com.juandroiddev.chirp.infra.message_queue

import com.juandroiddev.chirp.domain.events.chat.ChatEvent
import com.juandroiddev.chirp.service.PushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationChatEventListener(
    private val pushNotificationService: PushNotificationService
) {


    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_CHAT_EVENTS])
    @Transactional
    fun handleUserEvent(event: ChatEvent) {
        logger.info("Received chat event: ${event.eventKey} with ID: ${event.eventId}")
        
        when (event) {
            is ChatEvent.NewMessage -> {
                logger.info( "Processing NewMessage event for chatId: ${event.chatId}, senderId: ${event.senderId}")
                pushNotificationService.sendNewMessageNotification(
                    recipientUserIds = event.recipients.toList(),
                    senderUserId = event.senderId,
                    senderUserName = event.senderUserName,
                    message = event.message,
                    chatId = event.chatId
                )
            }
        }
    }
}