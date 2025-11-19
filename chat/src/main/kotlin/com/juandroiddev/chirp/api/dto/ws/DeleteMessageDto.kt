package com.juandroiddev.chirp.api.dto.ws

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.ChatMessageId

data class DeleteMessageDto@JsonCreator constructor(
    @JsonProperty("chatId")
    val chatId: ChatId,
    @JsonProperty("messageId")
    val messageId: ChatMessageId
)
