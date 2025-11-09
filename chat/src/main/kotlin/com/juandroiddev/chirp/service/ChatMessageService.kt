package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.api.dto.ChatMessageDto
import com.juandroiddev.chirp.api.mappers.toChatMessageDto
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
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class ChatMessageService (
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository
){

    @Transactional
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

        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                id = messageId,
                chatId = chat.id!!,
                chat = chat,
                sender = sender,
                content = content.trim()
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
    }
}