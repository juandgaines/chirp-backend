package com.juandroiddev.chirp.api.dto.ws

import com.juandroiddev.chirp.domain.type.UserId

class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?
)