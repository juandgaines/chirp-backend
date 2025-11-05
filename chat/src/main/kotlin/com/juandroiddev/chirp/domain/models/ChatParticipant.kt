package com.juandroiddev.chirp.domain.models

import com.juandroiddev.chirp.domain.type.UserId


data class ChatParticipant (
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)