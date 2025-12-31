package com.juandroiddev.chirp.api.controllers

import com.juandroiddev.chirp.api.dto.DeviceTokenDto
import com.juandroiddev.chirp.api.dto.RegisterDeviceRequest
import com.juandroiddev.chirp.api.mappers.toDeviceTokenDto
import com.juandroiddev.chirp.api.mappers.toPlatform
import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(
    private val pushNotificationService: PushNotificationService
) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid
        @RequestBody
        body: RegisterDeviceRequest
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platform.toPlatform()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/{token}")
    fun unregisterDeviceToken(
        @PathVariable("token") token: String
    ){
        pushNotificationService.unregisterDevice(
            token = token
        )
    }

}