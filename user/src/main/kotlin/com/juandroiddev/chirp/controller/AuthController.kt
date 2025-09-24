package com.juandroiddev.chirp.controller

import com.juandroiddev.chirp.api.dto.RegisterRequest
import com.juandroiddev.chirp.api.dto.UserDto
import com.juandroiddev.chirp.api.mappers.toUserDto
import com.juandroiddev.chirp.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
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
}