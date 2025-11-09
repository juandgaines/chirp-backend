package com.juandroiddev.chirp.api.controllers

import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.domain.type.ChatMessageId
import com.juandroiddev.chirp.service.ChatMessageService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/messages")
class ChatMessageController (
    private val chatMessageService: ChatMessageService
){

    @DeleteMapping("/{messageId}")
    fun deleteMessage(
        @PathVariable("messageId")
        messageId: ChatMessageId
    ) {
        chatMessageService.deleteMessage(
            messageId = messageId,
            requestUserId
        )
    }

}