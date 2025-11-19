package com.juandroiddev.chirp.api.dto.ws

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.juandroiddev.chirp.domain.type.ChatId

data class ChatParticipantChangedDto @JsonCreator constructor(
    @JsonProperty("chatId")
    val chatId: ChatId
)