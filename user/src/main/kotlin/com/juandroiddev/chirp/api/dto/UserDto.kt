package com.juandroiddev.chirp.api.dto

import com.juandroiddev.chirp.domain.model.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasEmailVerified: Boolean,
)