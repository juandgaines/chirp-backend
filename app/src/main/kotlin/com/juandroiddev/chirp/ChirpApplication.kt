package com.juandroiddev.chirp

import com.juandroiddev.chirp.infra.database.entities.UserEntity
import com.juandroiddev.chirp.infra.database.repositories.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class ChirpApplication

fun main(args: Array<String>) {
	runApplication<ChirpApplication>(*args)
}