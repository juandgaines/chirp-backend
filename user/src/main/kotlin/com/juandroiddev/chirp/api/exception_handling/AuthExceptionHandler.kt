package com.juandroiddev.chirp.api.exception_handling

import com.juandroiddev.chirp.domain.exception.EmailNotVerifiedException
import com.juandroiddev.chirp.domain.exception.InvalidCredentialsException
import com.juandroiddev.chirp.domain.exception.InvalidTokenException
import com.juandroiddev.chirp.domain.exception.RateLimitException
import com.juandroiddev.chirp.domain.exception.SamePasswordException
import com.juandroiddev.chirp.domain.exception.UnauthorizedException
import com.juandroiddev.chirp.domain.exception.UserAlreadyExistsException
import com.juandroiddev.chirp.domain.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun onUserAlreadyExistsException(
        e: UserAlreadyExistsException
    ): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                mapOf(
                    "code" to "USER_EXISTS",
                    "status" to e.message
                )
            )
    }

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onUserNotFound(
        e: UserNotFoundException
    ) = mapOf(
        "code" to "USER_NOT_FOUND",
        "status" to e.message
    )


    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidTokenException(
        e: InvalidTokenException
    ) = mapOf(
        "code" to "INVALID_TOKEN",
        "status" to e.message
    )

    @ExceptionHandler(RateLimitException::class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    fun onRateLimit(
        e: RateLimitException
    ) = mapOf(
        "code" to "RATE_LIMIT_EXCEEDED",
        "status" to e.message
    )

    @ExceptionHandler(EmailNotVerifiedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onEmailNotVerified(
        e: EmailNotVerifiedException
    ) = mapOf(
        "code" to "EMAIL_NOT_VERIFIED",
        "status" to e.message
    )

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onUnauthorized(
        e: UnauthorizedException
    ) = mapOf(
        "code" to "UNAUTHORIZED",
        "status" to e.message
    )

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onInvalidTokenException(
        e: InvalidCredentialsException
    ) = mapOf(
        "code" to "INVALID_CREDENTIALS",
        "status" to e.message
    )

    @ExceptionHandler(SamePasswordException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onInvalidTokenException(
        e: SamePasswordException
    ) = mapOf(
        "code" to "SAME_PASSWORD",
        "status" to e.message
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(
        e: MethodArgumentNotValidException
    ): ResponseEntity<Map<String,Any>>{
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage?: "Invalid value"

        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "code" to "VALIDATION_ERROR",
                "errors" to errors
            ))
    }
}