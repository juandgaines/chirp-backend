package com.juandroiddev.chirp.controller

import com.juandroiddev.chirp.api.config.IpRateLimit
import com.juandroiddev.chirp.api.dto.*
import com.juandroiddev.chirp.api.mappers.toAuthenticatedUserDto
import com.juandroiddev.chirp.api.mappers.toUserDto
import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.domain.model.UserId
import com.juandroiddev.chirp.infra.rate_limiting.EmailRateLimiter
import com.juandroiddev.chirp.service.AuthService
import com.juandroiddev.chirp.service.EmailVerificationService
import com.juandroiddev.chirp.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController (
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter
){

    @PostMapping("/register")
    @IpRateLimit(
        duration = 1L,
        unit = TimeUnit.HOURS,
        requests = 10
    )
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
    @IpRateLimit(
        duration = 1L,
        unit = TimeUnit.HOURS,
        requests = 10
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
    @IpRateLimit(
        duration = 1L,
        unit = TimeUnit.HOURS,
        requests = 10
    )
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
    @PostMapping("/resend-verification")
    @IpRateLimit(
        duration = 1L,
        unit = TimeUnit.HOURS,
        requests = 10
    )
    fun resendVerification(
        @Valid
        @RequestBody
        body: EmailRequest
    ){
        emailRateLimiter.withRateLimit(body.email){
            emailVerificationService.resendVerification(body.email)
        }
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
    @PostMapping("/forgot-password")
    @IpRateLimit(
        duration = 1L,
        unit = TimeUnit.HOURS,
        requests = 10
    )
    fun forgotPassword(
        @Valid
        @RequestBody
        body: EmailRequest
    ){
        passwordResetService.requestPasswordReset(
            email = body.email
        )
    }



    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid
        @RequestBody
        body: ResetPasswordRequest
    ){
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid
        @RequestBody
        body: ChangePasswordRequest
    ){
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword
        )
    }

}