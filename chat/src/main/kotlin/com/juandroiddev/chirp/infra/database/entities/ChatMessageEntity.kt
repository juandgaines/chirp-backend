package com.juandroiddev.chirp.infra.database.entities

import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.ChatMessageId
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant


@Entity
@Table(
    name = "chat_messages",
    schema = "chat_service",
    indexes = [
        Index(
            name = "idx_chat_messages_chat_id_created_at",
            columnList = "chat_id, createdAt DESC"
        )
    ]
)
class ChatMessageEntity (
    @Id
    var id: ChatMessageId? = null,
    @Column(nullable = false)
    var content: String,
    @Column(
        name = "chat_id",
        nullable = false,
        updatable = false
    )
    var chatId: ChatId,
    @ManyToOne
    @JoinColumn(
        name = "chat_id",
        nullable = false,
        insertable = false,
        updatable = false
    )
    @OnDelete( action =  OnDeleteAction.CASCADE)
    var chat: ChatEntity? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
    )
    var sender : ChatParticipantEntity,
    @Column(name = "created_at")
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)