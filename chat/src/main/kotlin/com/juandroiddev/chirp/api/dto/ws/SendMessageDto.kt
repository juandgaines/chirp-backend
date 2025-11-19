package com.juandroiddev.chirp.api.dto.ws

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.ChatMessageId

data class SendMessageDto @JsonCreator  constructor (
    @JsonProperty
    val chatId: ChatId,
    @JsonProperty("content")
    val content: String,
    @JsonProperty("messageId")
    val messageId: ChatMessageId? = null,
)