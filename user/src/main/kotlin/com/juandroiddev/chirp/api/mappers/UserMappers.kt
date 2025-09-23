package com.juandroiddev.chirp.api.mappers

import com.juandroiddev.chirp.api.dto.AuthenticatedUserDto
import com.juandroiddev.chirp.api.dto.UserDto
import com.juandroiddev.chirp.domain.model.AuthenticatedUser
import com.juandroiddev.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto() = AuthenticatedUserDto(
    user = user.toUserDto(),
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun User.toUserDto() = UserDto(
    id = id,
    email = email,
    username = username,
    hasEmailVerified = hasEmailVerified
)