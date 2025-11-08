package com.juandroiddev.chirp.api.controllers

import com.juandroiddev.chirp.api.dto.AddParticipantToChatDto
import com.juandroiddev.chirp.api.dto.ChatDto
import com.juandroiddev.chirp.api.dto.CreateChatRequest
import com.juandroiddev.chirp.api.mappers.toChatDto
import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

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
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = request.otherUsersIds.toSet()
        ).toChatDto()
    }

    @PostMapping("/{chatId}/add")
    fun addParticipantsToChat(
        @PathVariable
        chatId: ChatId,
        @Valid
        @RequestBody
        request: AddParticipantToChatDto,
    ): ChatDto {
        return chatService.addParticipantToChat(
             requestUserId = requestUserId,
            chatId =  chatId,
            request.userIds.toSet()
        ).toChatDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun leaveChat(
        @PathVariable
        chatId: ChatId,
    ) {
        chatService.removeParticipantFromChat(
            userId = requestUserId,
            chatId =  chatId,
        )
    }
}