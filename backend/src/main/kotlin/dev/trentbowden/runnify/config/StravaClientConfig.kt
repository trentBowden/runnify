package dev.trentbowden.runnify.config

import dev.trentbowden.runnify.strava.client.api.ActivitiesApi
import dev.trentbowden.runnify.strava.client.infrastructure.ApiClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StravaClientConfig {

    @Value("\${strava.client-id}")
    private lateinit var clientId: String

    @Value("\${strava.client-secret}")
    private lateinit var clientSecret: String

    @Bean
    fun stravaApiClient(): ApiClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = ApiClient(
            baseUrl = "https://www.strava.com/api/v3",
            okHttpClientBuilder = OkHttpClient.Builder()
                .addInterceptor(logging)
            )

        return client
    }

    @Bean
    fun stravaActivitiesApi(apiClient: ApiClient): ActivitiesApi {
        return ActivitiesApi(apiClient)
    }
}