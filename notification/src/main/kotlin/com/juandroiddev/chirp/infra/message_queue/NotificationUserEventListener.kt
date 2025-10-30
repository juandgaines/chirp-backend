package com.juandroiddev.chirp.infra.message_queue

import com.juandroiddev.chirp.domain.events.user.UserEvent
import com.juandroiddev.chirp.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Component
class NotificationUserEventListener(
    private val emailService: EmailService
) {


    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        logger.info("Received user event: ${event.eventKey} with ID: ${event.eventId}")
        
        when (event) {
            is UserEvent.Created ->{
                logger.info("Processing user created event for user: ${event.userId}")
                emailService.sendVerificationEmail(
                    email = event.email,
                    userName = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )

            }
            is UserEvent.RequestResendVerification -> {
                logger.info("Processing resend verification request for user: ${event.userId}")
                emailService.sendVerificationEmail(
                    email = event.email,
                    userName = event.username,
                    userId = event.userId,
                    token = event.verificationToken
                )
            }
            is UserEvent.RequestResetPassword -> {
                logger.info("Processing password reset request for user: ${event.userId}")
                emailService.sendPasswordResetEmail(
                    email = event.email,
                    userName = event.username,
                    userId = event.userId,
                    token = event.passwordResetToken,
                    expiresIn = Duration.ofMinutes(event.expiresInMinutes)
                )
            }
            else -> logger.warn("Received unknown user event type: ${event::class.simpleName}")
        }
    }
}