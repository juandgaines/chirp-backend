package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.events.user.UserEvent
import com.juandroiddev.chirp.domain.exception.EmailNotVerifiedException
import com.juandroiddev.chirp.domain.exception.InvalidCredentialsException
import com.juandroiddev.chirp.domain.exception.InvalidTokenException
import com.juandroiddev.chirp.domain.exception.PasswordEncodeException
import com.juandroiddev.chirp.domain.exception.UserAlreadyExistsException
import com.juandroiddev.chirp.domain.exception.UserNotFoundException
import com.juandroiddev.chirp.domain.model.AuthenticatedUser
import com.juandroiddev.chirp.domain.model.User
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.entities.RefreshTokenEntity
import com.juandroiddev.chirp.infra.database.entities.UserEntity
import com.juandroiddev.chirp.infra.database.repositories.RefreshTokenRepository
import com.juandroiddev.chirp.infra.database.repositories.UserRepository
import com.juandroiddev.chirp.infra.database.mappers.toUser
import com.juandroiddev.chirp.infra.message_queue.EventPublisher
import com.juandroiddev.chirp.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JWTService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher
){
    @Transactional
    fun register(email: String, username: String, password: String): User {
        val trimmedEmail = email.trim()
        val user = userRepository.findByEmailOrUsername(
            email =trimmedEmail,
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


        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = trimmedEmail,
                username = username,
                hashedPassword = hashedPassword
            )
        ).toUser()
        val token = emailVerificationService.createVerificationToken(trimmedEmail)
        eventPublisher.publish(
            event = UserEvent.Created(
                userId = savedUser.id,
                email = savedUser.email,
                username = savedUser.username,
                verificationToken = token.token
            )
        )
        return savedUser
    }
    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser{
        if(!jwtService.validateRefreshToken(refreshToken)){
            throw InvalidTokenException(
                message = "Invalid refresh token"
            )
        }

        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        val hashed = hashToken(refreshToken)

        return user.id?.let {userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            )?: throw InvalidTokenException("Invalid refresh token")

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            )
            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(
                userId = userId,
                token = newRefreshToken
            )
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        }?: throw UserNotFoundException()

    }
    @Transactional
    fun logout(refreshToken: String){
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashed = hashToken(refreshToken)
        refreshTokenRepository.deleteByUserIdAndHashedToken(
            userId = userId,
            hashedToken = hashed
        )
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
        if (!user.hasEmailVerified){
            throw EmailNotVerifiedException()
        }
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

    private fun storeRefreshToken(userId: UserId, token:String){
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
       val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}