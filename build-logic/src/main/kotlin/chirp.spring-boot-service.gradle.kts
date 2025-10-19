import gradle.kotlin.dsl.accessors._6c35dc5b101437ffcd4f7834c90ef9dd.dependencyManagement
plugins{
    id("chirp.kotlin-common")
    id("io.spring.dependency-management")
    kotlin("plugin.jpa")
}

dependencies{
    "implementation"(libraries.findLibrary("kotlin-reflect").get())
    "implementation"(libraries.findLibrary("kotlin-stdlib").get())
    "implementation"(libraries.findLibrary("spring-boot-starter-web").get())
    "implementation"(libraries.findLibrary("spring-boot-starter-test").get())
    "implementation"(libraries.findLibrary("kotlin-test-junit5").get())
    "implementation"(libraries.findLibrary("junit-platform-launcher").get())
}