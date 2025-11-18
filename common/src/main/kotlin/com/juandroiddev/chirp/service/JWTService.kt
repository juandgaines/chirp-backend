package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.exception.InvalidTokenException
import com.juandroiddev.chirp.domain.type.UserId
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import kotlin.io.encoding.Base64

@Service
class JWTService(
    @param:Value("\${jwt.secret}") private val secretBase64: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int
) {
    private val sercretKey = Keys.hmacShaKeyFor(
        Base64.decode(secretBase64)
    )

    private val accessTokenValidityMS = expirationMinutes * 60 * 1000L
    val refreshTokenValidityMS = 60 * 24 * 7 * 60 * 1000L

    fun generateAccessToken(userId: UserId): String =
        generateToken(userId, type = "access", expire = accessTokenValidityMS)

    fun generateRefreshToken(userId: UserId): String =
        generateToken(userId, type = "refresh", expire = refreshTokenValidityMS)

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }
    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }
    fun getUserIdFromToken(token: String): UserId {
        val claims = parseAllClaims(token) ?: throw InvalidTokenException(
            message = "The attached JWT token is invalid"
        )
        return UUID.fromString(claims.subject)
    }
    private fun generateToken(
        userId: UserId,
        type: String,
        expire: Long
    ): String {
        val now = Date()
        val expireDate = Date(now.time + expire)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expireDate)
            .signWith(sercretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims?{
        val rawToken  = if (token.startsWith("Bearer ")){
            token.removePrefix("Bearer ")
        } else token
        return try {
            Jwts.parser()
                .verifyWith(sercretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception){
            null
        }
    }

}