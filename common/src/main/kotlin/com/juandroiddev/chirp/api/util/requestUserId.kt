package com.juandroiddev.chirp.api.util

import com.juandroiddev.chirp.domain.exception.UnauthorizedException
import com.juandroiddev.chirp.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId ?:
    throw UnauthorizedException()