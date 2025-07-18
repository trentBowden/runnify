package dev.trentbowden.runnify.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConfigurationProperties(prefix = "spotify")
data class SpotifyConfig(
    var clientId: String = "",
    var clientSecret: String = "",
    var redirectUri: String = "",
    var baseUrl: String = "https://api.spotify.com/v1"
) {
    
    @Bean
    fun spotifyWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
} 