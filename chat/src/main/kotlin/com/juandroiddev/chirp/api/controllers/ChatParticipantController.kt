package com.juandroiddev.chirp.api.controllers

import com.juandroiddev.chirp.api.dto.ChatParticipantDto
import com.juandroiddev.chirp.api.mappers.toChatParticipantDto
import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.service.ChatParticipantService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController(
    "api/chat/participants"
)
class ChatParticipantController (
    private val chatParticipantService: ChatParticipantService
){
    @GetMapping
    fun getChatParticipantByUsernameOrEmail(
        @RequestParam(required = false) query: String?
    ): ChatParticipantDto {
        val participant = if ( query  == null ){
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query)
        }

        return participant?.toChatParticipantDto()
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND
            )
    }
}