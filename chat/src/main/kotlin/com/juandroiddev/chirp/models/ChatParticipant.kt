package com.juandroiddev.chirp.models

import com.juandroiddev.chirp.domain.type.UserId


data class ChatParticipant (
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)