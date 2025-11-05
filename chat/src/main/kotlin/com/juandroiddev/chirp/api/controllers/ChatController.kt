package com.juandroiddev.chirp.api.controllers

import com.juandroiddev.chirp.api.dto.ChatDto
import com.juandroiddev.chirp.api.dto.CreateChatRequest
import com.juandroiddev.chirp.api.mappers.toChatDto
import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChat(
        @Valid
        @RequestBody
        request: CreateChatRequest
    ): ChatDto {
        // Implementation goes here
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = request.otherUsersIds.toSet()
        ).toChatDto()
    }
}