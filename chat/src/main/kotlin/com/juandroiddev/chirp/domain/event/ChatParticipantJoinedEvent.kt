package com.juandroiddev.chirp.domain.event

import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.UserId

data class ChatParticipantJoinedEvent(
    val chatId: ChatId,
    val userId: Set<UserId>
)