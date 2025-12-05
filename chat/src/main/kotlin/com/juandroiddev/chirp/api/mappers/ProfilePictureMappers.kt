package com.juandroiddev.chirp.api.mappers

import com.juandroiddev.chirp.api.dto.PictureUploadResponse
import com.juandroiddev.chirp.domain.models.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse() = PictureUploadResponse(
    uploadUrl = this.uploadUrl,
    publicUrl = this.publicUrl,
    header = this.header,
    expiresAt = this.expiresAt
)