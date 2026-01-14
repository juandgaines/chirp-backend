package com.juandroiddev.chirp.domain.event

import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.UserId

data class ChatCreatedEvent(
    val chatId: ChatId,
    val participantsIds : List<UserId>
)