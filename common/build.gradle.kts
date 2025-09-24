plugins {
    id("java-library")
    id("chirp.kotlin-common")
}

group = "org.juandroiddev"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {

    api(libs.kotlin.reflect)
    api(libs.jackson.module.kotlin)
    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}