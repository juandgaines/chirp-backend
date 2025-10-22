package com.juandroiddev.chirp.infra.message_queue

import com.juandroiddev.chirp.domain.events.ChirpEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun <T: ChirpEvent> publish(event: T) {
        try{
            rabbitTemplate.convertAndSend(
                event.eventKey,
                event.eventKey,
                event
            )
            logger.info("Successfully published event: ${event.eventKey}")
        }catch (e: Exception) {
            logger.error("Failed to publish event: ${event.eventKey}", e)
        }
    }
}