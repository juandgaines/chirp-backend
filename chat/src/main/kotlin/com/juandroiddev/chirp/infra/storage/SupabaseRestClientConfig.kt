package com.juandroiddev.chirp.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SupabaseRestClientConfig (
    @param:Value("\${supabase.url}") private val supabaseUrl: String,
    @param:Value("\${supabase.service-key}") private val supabaseServiceKey: String
){

    @Bean
    fun supabaseRestClient(): RestClient {
        val restClient = RestClient.builder()
            .baseUrl(supabaseUrl)
            .defaultHeader("Authorization ", "Bearer $supabaseServiceKey")
            .defaultHeader("Content-Type", "application/json")
            .build()
        return restClient
    }
}