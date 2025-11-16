package com.juandroiddev.chirp.domain.events.chat

import com.juandroiddev.chirp.domain.events.ChirpEvent
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.UserId
import java.time.Instant
import java.util.UUID

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now()
): ChirpEvent {

    data class NewMessage(
        val senderId : UserId,
        val senderUserName: String,
        val recipients: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE
    ): ChatEvent(), ChirpEvent

}