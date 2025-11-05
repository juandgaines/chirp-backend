package com.juandroiddev.chirp.domain.models

import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.ChatMessageId
import java.time.Instant

data class ChatMessage(
    val id: ChatMessageId,
    val chat: ChatId,
    val sender: ChatParticipant,
    val content:String,
    val createdAt: Instant
)