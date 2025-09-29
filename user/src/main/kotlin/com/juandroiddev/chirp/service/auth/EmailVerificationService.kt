package com.juandroiddev.chirp.service.auth

import com.juandroiddev.chirp.domain.exception.InvalidTokenException
import com.juandroiddev.chirp.domain.exception.UserNotFoundException
import com.juandroiddev.chirp.domain.model.EmailVerificationToken
import com.juandroiddev.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.juandroiddev.chirp.infra.database.mappers.toEmailVerificationToken
import com.juandroiddev.chirp.infra.database.mappers.toUser
import com.juandroiddev.chirp.infra.database.repositories.EmailVerificationTokenRepository
import com.juandroiddev.chirp.infra.database.repositories.UserRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Service
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.expiry-hours}")
    private val expiryHours:Long
) {
    @Transactional
    fun createVerification(email: String): EmailVerificationToken{
        val userEntity = userRepository.findByEmail(email)
            ?: throw UserNotFoundException()

        val existingTokens = emailVerificationTokenRepository.findByUserAndUsedAtIsNull(
            userEntity
        )

        val now = Instant.now()
        val usedTokens = existingTokens.map {
            it.apply {
                usedAt = now
            }
        }

        emailVerificationTokenRepository.saveAll(
            usedTokens
        )

        val token = EmailVerificationTokenEntity(
          expiresAt = now.plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )

        return emailVerificationTokenRepository.save(
            token
        ).toEmailVerificationToken()

    }

    @Transactional
    fun verifyEmail(token : String){

        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid.")


        if(verificationToken.isUsed){
           throw InvalidTokenException(
               "Email verification token is already used."
           )
        }

        if(verificationToken.isExpired){
            throw InvalidTokenException(
                "Email verification token has already expired."
            )
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                this.usedAt = Instant.now()
            }
        )

        userRepository.save(
            verificationToken.user.apply {
                this.hasEmailVerified = true
            }
        ).toUser()
    }


    @Scheduled(
        cron = "0 0 3 * * *"
    )
    fun cleanUpExpiredTokens(){
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}