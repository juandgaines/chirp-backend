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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
) {
    companion object{
        private  val RETRY_DELAY_SECONDS = listOf(
            30L,
            60L,
            120L,
            300L,
            600L
        )
        const val MAX_RETRY_AGE_MINUTES = 30L
    }
    private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()

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
        sendWithRetry(
            notification = notification,
            attempt = 0
        )
    }

    fun sendWithRetry(
        notification: PushNotification,
        attempt: Int
    ){
        val result = firebasePushNotificationService.sendNotification(notification)

        result.permanentFailures.forEach {
            deviceTokenRepository.deleteByToken(it.token)
        }

        if(result.temporaryFailures.isNotEmpty() && attempt < RETRY_DELAY_SECONDS.size){
           val retryNotification = notification.copy(
               recipients = result.temporaryFailures
           )
            scheduleRetry(
                notification = retryNotification,
                attempt = attempt + 1
            )
        }
        if ( result.succeeded.isNotEmpty()){
            logger.info("Successfully sent notification to  ${result.succeeded.size} devices.")
        }

    }

    private fun scheduleRetry(
        notification: PushNotification,
        attempt: Int
    ){
        val delaySeconds = RETRY_DELAY_SECONDS.getOrElse(attempt -1){
            RETRY_DELAY_SECONDS.last()
        }

        val execureAt = Instant.now().plusSeconds(delaySeconds)
        val executeMillis = execureAt.toEpochMilli()
        val retryData = RetryData(
            notification = notification,
            attempt = attempt,
            createdAt = Instant.now()
        )
        retryQueue.compute(executeMillis){_, retries ->
            (retries ?: mutableListOf()).apply { add(retryData) }
        }
        logger.info("Scheduled retry #$attempt for ${notification.id} devices in $delaySeconds seconds.")
    }

    @Scheduled(fixedDelay = 15_000L)
    fun processRetries(){
        val now = Instant.now()
        val nowMillis = now.toEpochMilli()

        val toProcess = retryQueue.headMap(nowMillis, true)

        if (toProcess.isEmpty()){
            return
        }

        val entries = toProcess.entries.toList()

        entries.forEach { (timeMillis, retries) ->
            retryQueue.remove(timeMillis)

            retries.forEach { retry ->
                try{
                    val age = Duration.between(retry.createdAt, now)
                    if (age.toMinutes() > MAX_RETRY_AGE_MINUTES){
                        logger.info("Discarding retry for notification ${retry.notification.id} due to age ${age.toMinutes()} minutes.")
                        return@forEach
                    }

                    sendWithRetry(
                        notification = retry.notification,
                        attempt = retry.attempt
                    )

                }catch (e: Exception){
                    logger.warn("Failed to process retry for notification ${retry.notification.id}", e)
                }
            }
        }

    }

    private data class RetryData(
        val notification: PushNotification,
        val attempt: Int,
        val createdAt: Instant
    )
}