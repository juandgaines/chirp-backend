package com.juandroiddev.chirp.controller

import com.juandroiddev.chirp.api.dto.AuthenticatedUserDto
import com.juandroiddev.chirp.api.dto.LoginRequest
import com.juandroiddev.chirp.api.dto.RefreshRequest
import com.juandroiddev.chirp.api.dto.RegisterRequest
import com.juandroiddev.chirp.api.dto.UserDto
import com.juandroiddev.chirp.api.mappers.toAuthenticatedUserDto
import com.juandroiddev.chirp.api.mappers.toUserDto
import com.juandroiddev.chirp.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController (
    private val authService: AuthService
){

    @PostMapping("/register")
    fun register(
        @Valid
        @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
    }

    @PostMapping(
        "/login"
    )
    fun login(
        @RequestBody
        body: LoginRequest
    ): AuthenticatedUserDto{
        return authService.login(
            usernameOrEmail = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return  authService.refresh(
            body.refreshToken
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody
        body: RefreshRequest
    ){
        authService.logout(
            body.refreshToken
        )
    }
}