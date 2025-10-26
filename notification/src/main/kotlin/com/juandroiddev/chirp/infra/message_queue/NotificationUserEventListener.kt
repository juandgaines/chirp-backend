package com.juandroiddev.chirp.infra.message_queue

import com.juandroiddev.chirp.domain.events.user.UserEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationUserEventListener {
    
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        logger.info("Received user event: ${event.eventKey} with ID: ${event.eventId}")
        
        when (event) {
            is UserEvent.Created ->{
                logger.info("Processing user created event for user: ${event.userId}")
            }
            is UserEvent.RequestResendVerification -> {
                logger.info("Processing resend verification request for user: ${event.userId}")
            }
            is UserEvent.RequestResetPassword -> {
                logger.info("Processing password reset request for user: ${event.userId}")
            }
            else -> logger.warn("Received unknown user event type: ${event::class.simpleName}")
        }
    }
}