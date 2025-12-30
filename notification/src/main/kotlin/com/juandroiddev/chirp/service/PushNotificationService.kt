package com.juandroiddev.chirp.service

import com.juandroiddev.chirp.domain.DeviceToken
import com.juandroiddev.chirp.domain.PushNotification
import com.juandroiddev.chirp.domain.exception.InvalidDeviceTokenException
import com.juandroiddev.chirp.domain.type.ChatId
import com.juandroiddev.chirp.domain.type.UserId
import com.juandroiddev.chirp.infra.database.DeviceTokenEntity
import com.juandroiddev.chirp.infra.database.DeviceTokenRepository
import com.juandroiddev.chirp.infra.mappers.toDeviceToken
import com.juandroiddev.chirp.infra.mappers.toPlatformEntity
import com.juandroiddev.chirp.infra.push_notification.FirebasePushNotificationService
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    @Transactional
    fun registerDevice(
        userId: UserId,
        token: String,
        platform: DeviceToken.Platform
    ): DeviceToken{

        val existing = deviceTokenRepository.findByToken(token)

        val trimmedToken = token.trim()
        if(existing!= null && firebasePushNotificationService.isValidToken(trimmedToken)){
            throw InvalidDeviceTokenException()
        }
        val entity = if (existing != null){
            deviceTokenRepository.save(
                existing.apply {
                    this.userId = userId
                }
            )
        } else {
            deviceTokenRepository.save(
                DeviceTokenEntity(
                    userId = userId,
                    token = trimmedToken,
                    platform = platform.toPlatformEntity()
                )
            )
        }
        return entity.toDeviceToken()
    }

    @Transactional
    fun unregisterDevice(token: String){
        deviceTokenRepository.deleteByToken(token.trim())
    }

    fun sendNewMessageNotification(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        senderUserName:String,
        message:String,
        chatId: ChatId
    ){
        val deviceTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)

        if (deviceTokens.isEmpty()){
            logger.info("No device tokens found for users: $recipientUserIds")
            return
        }
        val recipients = deviceTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }

        val notification = PushNotification(
            title = "New message from $senderUserName",
            recipients = recipients,
            message = message,
            chatId = chatId,
            data = mapOf(
                "chatId" to chatId.toString(),
                "type" to "new_message"
            )
        )
        firebasePushNotificationService.sendNotification(
            notification
        )
    }
}