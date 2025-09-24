package com.juandroiddev.chirp.api.exception_handling

import com.juandroiddev.chirp.domain.exception.UserAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
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