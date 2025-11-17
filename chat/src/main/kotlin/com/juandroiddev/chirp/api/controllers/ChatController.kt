package com.juandroiddev.chirp.api.controllers

import com.juandroiddev.chirp.api.dto.AddParticipantToChatDto
import com.juandroiddev.chirp.api.dto.ChatDto
import com.juandroiddev.chirp.api.dto.ChatMessageDto
import com.juandroiddev.chirp.api.dto.CreateChatRequest
import com.juandroiddev.chirp.api.mappers.toChatDto
import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant


@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    companion object{
        private const val DEFAULT_PAGE_SIZE = 20
    }

    @GetMapping("/{chatId}/messages")
    fun getMessagesForChat(
        @PathVariable(name = "chatId")
        chatId: ChatId,
        @RequestParam(name = "before",required = false) before: Instant? = null,
        @RequestParam(name = "pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<ChatMessageDto> {
        return chatService.getChatMessage(
            chatId = chatId,
            before = before,
            pageSize = pageSize
        )
    }

    @GetMapping("/{chatId}")
    fun getChat(
        @PathVariable
        chatId: ChatId,
    ): ChatDto {
        return chatService.getChatById(
            chatId = chatId,
            requestUserId = requestUserId
        )?.toChatDto()?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Chat with id $chatId not found."
        )
    }

    @GetMapping
    fun getChatsForUser(): List<ChatDto>{
        return chatService.findChatsByUserId(
            userId = requestUserId,
        ).map { it.toChatDto() }
    }

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