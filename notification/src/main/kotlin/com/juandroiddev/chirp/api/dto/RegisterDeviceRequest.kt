package com.juandroiddev.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class RegisterDeviceRequest @JsonCreator constructor(
    @field:NotBlank
    @JsonProperty("token")
    val token: String,
    val platform: PlatformDto

)

enum class PlatformDto {
    ANDROID,
    IOS
}