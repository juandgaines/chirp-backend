package com.juandroiddev.chirp.infra.database.mappers

import com.juandroiddev.chirp.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken() = com.juandroiddev.chirp.domain.model.EmailVerificationToken(
    id = id,
    token = token,
    user = user.toUser()
)