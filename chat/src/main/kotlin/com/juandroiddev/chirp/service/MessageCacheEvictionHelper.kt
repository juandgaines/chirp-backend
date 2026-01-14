package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.type.ChatId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class MessageCacheEvictionHelper {
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun evictMessageCache(
        chatId: ChatId
    ){
        //NO-OP: Let spring handle the cache eviction
    }
}