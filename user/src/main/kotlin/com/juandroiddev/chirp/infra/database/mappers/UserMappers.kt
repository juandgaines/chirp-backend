package com.juandroiddev.chirp.infra.database.mappers

import com.juandroiddev.chirp.domain.model.User
import com.juandroiddev.chirp.infra.database.entities.UserEntity

fun UserEntity.toUser() = User(
    id = id!!,
    email = email,
    username = username,
    hasEmailVerified = hasEmailVerified,
)