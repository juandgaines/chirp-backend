package com.juandroiddev.chirp.domain.exception

import com.juandroiddev.chirp.domain.type.ChatMessageId

class MessageNotFoundException(
    private val messageId: ChatMessageId
): RuntimeException(
    "Message with ID: $messageId was not found."
) {
}