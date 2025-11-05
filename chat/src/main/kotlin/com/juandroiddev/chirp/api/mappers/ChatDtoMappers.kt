package com.juandroiddev.chirp.api.mappers

import com.juandroiddev.chirp.api.dto.ChatDto
import com.juandroiddev.chirp.api.dto.ChatMessageDto
import com.juandroiddev.chirp.api.dto.ChatParticipantDto
import com.juandroiddev.chirp.domain.models.Chat
import com.juandroiddev.chirp.domain.models.ChatMessage
import com.juandroiddev.chirp.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map {
            it.toChatParticipantDto()
        },
        lastActivity =  lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}


fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto{
    return ChatParticipantDto(
        userId = userId,
        username = this.username,
        email = this.email,
        profilePictureUrl = this.profilePictureUrl
    )
}