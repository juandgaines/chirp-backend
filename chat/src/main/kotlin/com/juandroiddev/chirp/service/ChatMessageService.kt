package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.event.MessageDeletedEvent
import com.juandroiddev.chirp.domain.events.chat.ChatEvent
import com.juandroiddev.chirp.domain.exception.ChatNotFoundException
import com.juandroiddev.chirp.domain.exception.ChatParticipantNotFoundException
import com.juandroiddev.chirp.domain.exception.ForbiddenException
import com.juandroiddev.chirp.domain.exception.MessageNotFoundException
import com.juandroiddev.chirp.domain.models.ChatMessage
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.ChatMessageId
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.entities.ChatMessageEntity
import com.juandroiddev.chirp.infra.database.mappers.toChatMessage
import com.juandroiddev.chirp.infra.database.repositories.ChatMessageRepository
import com.juandroiddev.chirp.infra.database.repositories.ChatParticipantRepository
import com.juandroiddev.chirp.infra.database.repositories.ChatRepository
import com.juandroiddev.chirp.infra.message_queue.EventPublisher
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class ChatMessageService (
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher,
    private val messageCacheEvictionHelper: MessageCacheEvictionHelper
){

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()

        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId ?: UUID.randomUUID(),
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender
            )
        )

        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUserName = sender.username,
                recipients = chat.participants.map { it.userId}.toSet(),
                chatId = chatId,
                message = savedMessage.content
            )
        )

        return savedMessage.toChatMessage()
    }


    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }
        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                messageId = messageId,
                chatId = message.chatId
            )
        )
        messageCacheEvictionHelper.evictMessageCache(message.chatId)
    }
}
