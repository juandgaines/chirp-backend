package com.juandroiddev.chirp.service.auth

import com.juandroiddev.chirp.domain.exception.InvalidCredentialsException
import com.juandroiddev.chirp.domain.exception.PasswordEncodeException
import com.juandroiddev.chirp.domain.exception.UserAlreadyExistsException
import com.juandroiddev.chirp.domain.exception.UserNotFoundException
import com.juandroiddev.chirp.domain.model.AuthenticatedUser
import com.juandroiddev.chirp.domain.model.User
import com.juandroiddev.chirp.domain.model.UserId
import com.juandroiddev.chirp.infra.database.entities.RefreshTokenEntity
import com.juandroiddev.chirp.infra.database.entities.UserEntity
import com.juandroiddev.chirp.infra.database.repositories.RefreshTokenRepository
import com.juandroiddev.chirp.infra.database.repositories.UserRepository
import com.juandroiddev.chirp.infra.database.toUser
import com.juandroiddev.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JWTService
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

    fun login(
        usernameOrEmail: String, password: String
    ): AuthenticatedUser {
        val user = userRepository.findByEmail(
            email = usernameOrEmail.trim(),
        ) ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.hashedPassword)){
            throw InvalidCredentialsException()
        }
        //TODO: check for verified email


        return user.id?.let { userId->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)
            storeRefreshToken(
                userId = userId,
                token = refreshToken
            )
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }?: throw UserNotFoundException()
    }

    private fun storeRefreshToken(userId: UserId,token:String){
        val hashedToken = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMS
        val expiresAt = Instant.now().plusMillis(expiryMs)
        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                experiesAt = expiresAt,
                hashedToken = hashedToken
            )
        )

    }

    private fun hashToken(token:String):String{
       val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}