package com.juandroiddev.chirp.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import org.intellij.lang.annotations.RegExp

data class RegisterRequest(
    @field:Email
    val email: String,
    @field:Length(min = 3 , max = 20, message = "Username length must be between 3 and 20 characters")
    val username: String,
    @field:Pattern(
        regexp = "^(?=.*[\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(.{8,})$",
        message = "Password must be at least 8 characters and contain at least one digit or special character"
    )
    val password: String
)