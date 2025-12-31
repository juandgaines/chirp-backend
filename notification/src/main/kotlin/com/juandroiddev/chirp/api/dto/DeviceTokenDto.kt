package com.juandroiddev.chirp.api.dto

import com.juandroiddev.chirp.domain.type.UserId
import java.time.Instant

data class DeviceTokenDto(
    val userId: UserId,
    val token: String,
    val createdAt: Instant
)