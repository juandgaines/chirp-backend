package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.exception.ChatParticipantNotFoundException
import com.juandroiddev.chirp.domain.exception.InvalidChatSizeException
import com.juandroiddev.chirp.domain.models.Chat
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.entities.ChatEntity
import com.juandroiddev.chirp.infra.database.mappers.toChat
import com.juandroiddev.chirp.infra.database.repositories.ChatParticipantRepository
import com.juandroiddev.chirp.infra.database.repositories.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService (
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository
){

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipant = (otherParticipants + creatorId)
        if (allParticipant.size < 2){
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)


        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat()

    }
}