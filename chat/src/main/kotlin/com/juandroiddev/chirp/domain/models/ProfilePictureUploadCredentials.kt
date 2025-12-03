package com.juandroiddev.chirp.domain.models

import java.time.Instant

data class ProfilePictureUploadCredentials(
    val uploadUrl: String,
    val publicUrl: String,
    val header: Map<String, String>,
    val expiresAt : Instant
)