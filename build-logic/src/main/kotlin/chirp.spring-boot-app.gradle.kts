import gradle.kotlin.dsl.accessors._6c35dc5b101437ffcd4f7834c90ef9dd.allOpen
import gradle.kotlin.dsl.accessors._6c35dc5b101437ffcd4f7834c90ef9dd.java

plugins {
    id("chirp.spring-boot-service")
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

