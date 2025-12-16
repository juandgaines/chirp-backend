package com.juandroiddev.chirp.domain

data class PushNotificationSendResult(
    val succeeded:List<DeviceToken>,
    val temporaryFailures:List<DeviceToken>,
    val permanentFailures:List<DeviceToken>
)