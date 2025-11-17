package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.api.dto.ChatMessageDto
import com.juandroiddev.chirp.api.mappers.toChatMessageDto
import com.juandroiddev.chirp.domain.event.ChatParticipantJoinedEvent
import com.juandroiddev.chirp.domain.event.ChatParticipantLeftEvent
import com.juandroiddev.chirp.domain.exception.ChatNotFoundException
import com.juandroiddev.chirp.domain.exception.ChatParticipantNotFoundException
import com.juandroiddev.chirp.domain.exception.ForbiddenException
import com.juandroiddev.chirp.domain.exception.InvalidChatSizeException
import com.juandroiddev.chirp.domain.models.Chat
import com.juandroiddev.chirp.domain.models.ChatMessage
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.entities.ChatEntity
import com.juandroiddev.chirp.infra.database.mappers.toChat
import com.juandroiddev.chirp.infra.database.mappers.toChatMessage
import com.juandroiddev.chirp.infra.database.repositories.ChatMessageRepository
import com.juandroiddev.chirp.infra.database.repositories.ChatParticipantRepository
import com.juandroiddev.chirp.infra.database.repositories.ChatRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService (
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationPublisher: ApplicationEventPublisher
){

    @Cacheable (
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessage(
        chatId: ChatId,
        before: Instant? = null,
        pageSize: Int
    ): List<ChatMessageDto>{
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>
    ): Chat {
        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipants = setOf(creator) + otherParticipants
        if (allParticipants.size < 2){
            throw InvalidChatSizeException()
        }

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = allParticipants
            )
        ).toChat()

    }

    @Transactional
    fun addParticipantToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {

        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any {
            it.userId == requestUserId
        }

        if (!isRequestingUserInChat){
            throw ForbiddenException()
        }

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)?:
                throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                participants = participants + users
            }
        ).toChat(lastMessage = lastMessage )


        applicationPublisher.publishEvent(
            ChatParticipantJoinedEvent(
                chatId = chatId,
                userId = userIds
            )
        )
        return updatedChat
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository.findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()?.toChatMessage()
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {

        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val participant = chat.participants.find {
            it.userId == userId
        } ?: throw ChatParticipantNotFoundException(userId)

        val newParticipantSize = chat.participants.size -1
        if (newParticipantSize  == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(
            chat.apply {
                participants = chat.participants - participant
            }
        )
        applicationPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }
}