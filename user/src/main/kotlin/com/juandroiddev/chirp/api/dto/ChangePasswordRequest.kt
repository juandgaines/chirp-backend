package com.juandroiddev.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.juandroiddev.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest @JsonCreator constructor(
    @field:NotBlank
    @JsonProperty("oldPassword")
    val oldPassword: String,

    @field:Password
    @JsonProperty("newPassword")
    val newPassword: String
)