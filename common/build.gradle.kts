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
    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}