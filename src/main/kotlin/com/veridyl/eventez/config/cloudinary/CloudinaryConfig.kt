package com.veridyl.eventez.config.cloudinary

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudinaryConfig {

    @Value("\${cloudinary.cloud-name}")
    private lateinit var cloudName: String

    @Value("\${cloudinary.api-key}")
    private lateinit var apiKey: String

    @Value("\${cloudinary.api-secret}")
    private lateinit var apiSecret: String

    @Bean
    fun cloudinary(): Cloudinary {
        val config = mapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret,
            "secure" to "true"
        )
        return Cloudinary(config)
    }
}
