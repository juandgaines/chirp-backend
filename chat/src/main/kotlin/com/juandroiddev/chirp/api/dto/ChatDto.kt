package com.juandroiddev.chirp.api.dto

import com.juandroiddev.chirp.domain.type.ChatId
import java.time.Instant

data class ChatDto(
    val id: ChatId,
    val participants: List<ChatParticipantDto>,
    val lastActivity: Instant,
    val lastMessage: ChatMessageDto?,
    val creator: ChatParticipantDto?
)