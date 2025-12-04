package com.juandroiddev.chirp.domain.event

import com.juandroiddev.chirp.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?
)