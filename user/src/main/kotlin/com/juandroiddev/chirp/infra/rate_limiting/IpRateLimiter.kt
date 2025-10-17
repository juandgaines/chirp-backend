package com.juandroiddev.chirp.infra.rate_limiting

import com.juandroiddev.chirp.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IpRateLimiter (
    private val redisTemplate: StringRedisTemplate
){
    companion object {
        private const val IP_RATE_LIMIT_KEY_PREFIX = "rate_limit:ip"
    }

    @Value("classpath:ip_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.use {
            it.readBytes().decodeToString()
        }
        @Suppress("UNCHECKED_CAST")
        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }

    fun <T> withIpRateLimit(
        ipAddress:String,
        resetsIn: Duration,
        maxRequestPerIp: Int,
        action: () -> T
    ): T {
        val rateLimitKey = "$IP_RATE_LIMIT_KEY_PREFIX:$ipAddress"

        val result =  redisTemplate.execute(
            rateLimitScript,
            listOf(rateLimitKey),
            maxRequestPerIp.toString(),
            resetsIn.seconds.toString()
        )

        val currentCount = result[0]
        return if (currentCount <= maxRequestPerIp) {
            action()
        } else {
            val ttl = result[1]
            throw RateLimitException(
                resetsInSeconds = ttl
            )
        }
    }
}