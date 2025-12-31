package com.juandroiddev.chirp.api.mappers

import com.juandroiddev.chirp.api.dto.DeviceTokenDto
import com.juandroiddev.chirp.api.dto.PlatformDto
import com.juandroiddev.chirp.domain.DeviceToken

fun DeviceToken.toDeviceTokenDto() = DeviceTokenDto(
    userId = this.userId,
    token = this.token,
    createdAt = this.createdAt
)

fun PlatformDto.toPlatform(): DeviceToken.Platform = when(this){
    PlatformDto.ANDROID -> DeviceToken.Platform.ANDROID
    PlatformDto.IOS -> DeviceToken.Platform.IOS
}