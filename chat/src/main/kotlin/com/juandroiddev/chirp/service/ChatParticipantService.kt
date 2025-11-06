package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.models.ChatParticipant
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.mappers.toChatParticipant
import com.juandroiddev.chirp.infra.database.mappers.toChatParticipantEntity
import com.juandroiddev.chirp.infra.database.repositories.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService (
    private val chatParticipantRepository: ChatParticipantRepository
){

    fun createChatParticipant(
        chatParticipant: ChatParticipant
    ){
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(
        query: String
    ): ChatParticipant?{
        val normalizedQuery = query.trim().lowercase().trim()
        return  chatParticipantRepository.findByEmailOrUsername(
            query = normalizedQuery
        )?.toChatParticipant()
    }
}