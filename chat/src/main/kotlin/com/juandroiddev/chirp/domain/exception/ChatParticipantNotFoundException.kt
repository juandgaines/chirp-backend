package com.juandroiddev.chirp.domain.exception

import com.juandroiddev.chirp.domain.type.UserId

class ChatParticipantNotFoundException(
    private val id: UserId
): RuntimeException(
    "The chat participant with id $id was not found."
)