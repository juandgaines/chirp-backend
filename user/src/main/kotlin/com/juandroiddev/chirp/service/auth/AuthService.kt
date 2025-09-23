package com.juandroiddev.chirp.service.auth

import com.juandroiddev.chirp.domain.exception.PasswordEncodeException
import com.juandroiddev.chirp.domain.exception.UserAlreadyExistsException
import com.juandroiddev.chirp.domain.model.User
import com.juandroiddev.chirp.infra.database.entities.UserEntity
import com.juandroiddev.chirp.infra.database.repositories.UserRepository
import com.juandroiddev.chirp.infra.database.toUser
import com.juandroiddev.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
){
    fun register(email: String, username: String, password: String): User {

        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim()
        )

        if (user != null){
            throw UserAlreadyExistsException()
        }
        // BCryptPasswordEncoder.encode() can return null if input is null (see AbstractValidatingPasswordEncoder)
        // Throwing exception is valid approach - should map to HTTP 500 Internal Server Error
        // This is an internal server error, client should receive generic "Registration failed" message
        val hashedPassword = passwordEncoder.encode(password) ?:
            throw PasswordEncodeException()

        val savedUser = userRepository.save(
            UserEntity(
                email = email,
                username = username,
                hashedPassword = hashedPassword
            )
        ).toUser()

        return savedUser
    }
}