package com.juandroiddev.chirp.api.exception_handling

import com.juandroiddev.chirp.domain.exception.ChatNotFoundException
import com.juandroiddev.chirp.domain.exception.ChatParticipantNotFoundException
import com.juandroiddev.chirp.domain.exception.InvalidChatSizeException
import com.juandroiddev.chirp.domain.exception.MessageNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        MessageNotFoundException::class,
        ChatParticipantNotFoundException::class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleChatNotFoundException(e: Exception): Map<String, Any?> {
        return mapOf(
            "code" to "NOT_FOUND",
            "status" to e.message
        )
    }

    @ExceptionHandler(
        InvalidChatSizeException::class,
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidChatSize(e: InvalidChatSizeException): Map<String, Any?> {
        return mapOf(
            "code" to "INVALID_CHAT_SIZE",
            "status" to e.message
        )
    }
}