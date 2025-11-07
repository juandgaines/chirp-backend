package com.juandroiddev.chirp.domain.events.user

import com.juandroiddev.chirp.domain.events.ChirpEvent
import com.juandroiddev.chirp.domain.type.UserId
import java.time.Instant
import java.util.*

sealed class UserEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
): ChirpEvent{

    data class Created(
        val userId : UserId,
        val email : String,
        val username : String,
        val verificationToken : String,
        override val eventKey: String = UserEventConstants.USER_CREATED,
    ): UserEvent(), ChirpEvent

    data class Verified(
        val userId : UserId,
        val email : String,
        val username : String,
        override val eventKey: String = UserEventConstants.USER_VERIFIED,
    ): UserEvent(), ChirpEvent

    data class RequestResendVerification(
        val userId : UserId,
        val email : String,
        val username : String,
        val verificationToken : String,
        override val eventKey: String = UserEventConstants.USER_REQUESTED_RESEND_VERIFICATION,
    ): UserEvent(), ChirpEvent

    data class RequestResetPassword(
        val userId : UserId,
        val email : String,
        val username : String,
        val passwordResetToken : String,
        val expiresInMinutes: Long,
        override val eventKey: String = UserEventConstants.USER_REQUESTED_RESET_PASSWORD,
    ): UserEvent(), ChirpEvent

}