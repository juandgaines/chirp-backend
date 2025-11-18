package com.juandroiddev.chirp.api.dto.ws

enum class IncomingWebSocketEventType {
    NEW_MESSAGE,
}

enum class OutgoingWebSocketEventType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANT_CHANGED,
    ERROR,
}

data class IncomingWebSocketMessage(
    val type: IncomingWebSocketEventType,
    val payload: String,
)

data class OutgoingWebSocketMessage(
    val type: OutgoingWebSocketEventType,
    val payload: String,
)