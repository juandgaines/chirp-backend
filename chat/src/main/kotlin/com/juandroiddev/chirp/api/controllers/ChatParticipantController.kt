package com.juandroiddev.chirp.api.controllers

import com.juandroiddev.chirp.api.dto.ChatParticipantDto
import com.juandroiddev.chirp.api.dto.ConfirmProfilePictureRequest
import com.juandroiddev.chirp.api.dto.PictureUploadResponse
import com.juandroiddev.chirp.api.mappers.toChatParticipantDto
import com.juandroiddev.chirp.api.mappers.toResponse
import com.juandroiddev.chirp.api.util.requestUserId
import com.juandroiddev.chirp.service.ChatParticipantService
import com.juandroiddev.chirp.service.ProfilePictureService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("api/participants")
class ChatParticipantController (
    private val chatParticipantService: ChatParticipantService,
    private val profilePictureService: ProfilePictureService
){
    @GetMapping
    fun getChatParticipantByUsernameOrEmail(
        @RequestParam(required = false) query: String?
    ): ChatParticipantDto {
        val participant = if ( query  == null ){
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query)
        }

        return participant?.toChatParticipantDto()
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND
            )
    }

    @PostMapping("/profile-picture-upload")
    fun getProfilePictureUploadUrl(
        @RequestParam mimeType : String
    ): PictureUploadResponse{
        return profilePictureService.generateUploadCredentials(
            userId = requestUserId,
            mimeType = mimeType
        ).toResponse()
    }

    @PostMapping("/confirm-profile-picture")
    fun confirmProfilePictureUpload(
        @Valid
        @RequestBody body: ConfirmProfilePictureRequest
    ){
        return profilePictureService.confirmProfilePictureUpload(
            userId = requestUserId,
            publicUrl = body.publicUrl
        )
    }

    @DeleteMapping("/profile-picture")
    fun deleteProfilePicture(){
        profilePictureService.deleteProfilePicture(
            userId = requestUserId
        )
    }
}