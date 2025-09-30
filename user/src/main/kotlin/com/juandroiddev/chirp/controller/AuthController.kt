package com.juandroiddev.chirp.controller

import com.juandroiddev.chirp.api.dto.*
import com.juandroiddev.chirp.api.mappers.toAuthenticatedUserDto
import com.juandroiddev.chirp.api.mappers.toUserDto
import com.juandroiddev.chirp.service.AuthService
import com.juandroiddev.chirp.service.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController (
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
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
    @GetMapping(
        "/verify"
    )
    fun verifyEmail(
        @RequestParam
        token: String
    ){
        emailVerificationService.verifyEmail(token)
    }

}