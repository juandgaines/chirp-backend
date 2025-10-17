package com.juandroiddev.chirp.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("nginx")
data class NginxConfig(
    var trustedIps: List<String> = emptyList(),
    var requireProxy: Boolean = true
)