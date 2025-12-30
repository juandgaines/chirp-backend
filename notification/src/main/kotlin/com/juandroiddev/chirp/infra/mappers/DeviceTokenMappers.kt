package com.juandroiddev.chirp.infra.mappers

import com.juandroiddev.chirp.domain.DeviceToken
import com.juandroiddev.chirp.infra.database.DeviceTokenEntity


fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        userId = userId,
        token = token,
        platform = platform.toDomainPlatform(),
        createdAt = createdAt,
        id = id
    )
}