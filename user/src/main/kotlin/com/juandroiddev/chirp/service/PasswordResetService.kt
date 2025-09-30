package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.exception.*
import com.juandroiddev.chirp.domain.model.UserId
import com.juandroiddev.chirp.infra.database.entities.PasswordResetTokenEntity
import com.juandroiddev.chirp.infra.database.repositories.PasswordResetTokenRepository
import com.juandroiddev.chirp.infra.database.repositories.RefreshTokenRepository
import com.juandroiddev.chirp.infra.database.repositories.UserRepository
import com.juandroiddev.chirp.infra.security.PasswordEncoder
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService (
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}")
    private val expiryMinutes:Long,
    private val refreshTokenRepository: RefreshTokenRepository
){
    @Transactional
    fun requestPasswordReset(email: String) {

        val user = userRepository.findByEmail(email)
            ?: return // Silently return if user not found for security reasons

        passwordResetTokenRepository.invalidateActiveTokenForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )

        passwordResetTokenRepository.save(token)

        // TODO: Inform notification service to send email

    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid password reset token")

        if(resetToken.isUsed){
            throw InvalidTokenException(
                "Email rest token is already used."
            )
        }

        if(resetToken.isExpired){
            throw InvalidTokenException(
                "Email reset token has already expired."
            )
        }
        val user = resetToken.user
        if (passwordEncoder.matches(newPassword,user.hashedPassword)){
            throw SamePasswordException()
        }


        val hashedPassword = passwordEncoder.encode(newPassword)
            ?: throw PasswordEncodeException()

        userRepository.save(
            user.apply {
                this.hashedPassword = hashedPassword
            }
        )

        passwordResetTokenRepository.save(
            resetToken.apply {
                this.usedAt = Instant.now()
            }
        )

        refreshTokenRepository.deleteByUserId(
            userId = user.id!!
        )

    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ){
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword,user.hashedPassword)){
            throw InvalidCredentialsException()
        }

        if ( oldPassword == newPassword){
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(
            userId = user.id!!
        )

        val newHashedPassword = passwordEncoder.encode(newPassword)
            ?: throw PasswordEncodeException()

        userRepository.save(
            user.apply {
                this.hashedPassword = newHashedPassword
            }
        )
    }

    @Scheduled(
        cron = "0 0 3 * * *",
        zone = "UTC"
    )
    fun cleanUpExpiredTokens(){
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}