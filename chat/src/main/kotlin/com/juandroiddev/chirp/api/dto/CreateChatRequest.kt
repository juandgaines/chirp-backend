package com.juandroiddev.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.juandroiddev.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest @JsonCreator  constructor(
    @field:Size(min = 1,
        message = "Chat must have at least 2 unique participants."
    )
    @JsonProperty("otherUsersIds")
    val otherUsersIds: List<UserId>
)