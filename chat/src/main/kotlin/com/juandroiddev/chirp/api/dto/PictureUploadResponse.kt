package com.juandroiddev.chirp.api.dto

import java.time.Instant

data class PictureUploadResponse(
    val uploadUrl: String,
    val publicUrl: String,
    val header: Map<String, String>,
    val expiresAt : Instant
)