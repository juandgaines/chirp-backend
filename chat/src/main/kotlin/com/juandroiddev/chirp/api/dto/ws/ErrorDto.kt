package com.juandroiddev.chirp.api.dto.ws

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorDto  @JsonCreator constructor(
    @JsonProperty("code")
    val code: String,
    @JsonProperty("message")
    val message: String,
)
