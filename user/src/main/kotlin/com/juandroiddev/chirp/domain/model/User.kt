package com.juandroiddev.chirp.domain.model

import com.juandroiddev.chirp.domain.type.UserId


data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean
)