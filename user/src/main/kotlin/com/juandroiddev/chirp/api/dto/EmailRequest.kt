package com.juandroiddev.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.juandroiddev.chirp.api.util.Password
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailRequest @JsonCreator constructor(
    @field:Email
    @JsonProperty("email")
    val email: String,
)