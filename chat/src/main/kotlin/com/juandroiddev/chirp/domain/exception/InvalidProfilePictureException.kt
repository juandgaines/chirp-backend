package com.juandroiddev.chirp.domain.exception

class InvalidProfilePictureException(
    override val message: String? = null,
): RuntimeException (
    message ?: "The provided profile picture is invalid."
)