package dev.trentbowden.runnify.service.Strava

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.OAuthProvider
import dev.trentbowden.runnify.entity.OAuthState
import dev.trentbowden.runnify.entity.Playlist
import dev.trentbowden.runnify.repository.MemberRepository
import dev.trentbowden.runnify.repository.OAuthStateRepository
import dev.trentbowden.runnify.strava.client.api.ActivitiesApi
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.URI
import java.time.Instant

@Service
class StravaService(
    private val activitiesApi: ActivitiesApi,
    private val authApi: AuthApi,
    private val stravaCredentialsService: StravaCredentialsService
) {

    @Value("\${strava.client-id}")
    private lateinit var clientId: String

    @Value("\${strava.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${strava.redirect-uri}")
    private lateinit var redirectUri: String

    fun getAuthUrl(state: String): String {
        return "https://www.strava.com/oauth/authorize?" +
                "client_id=$clientId" +
                "&response_type=code" +
                "&redirect_uri=$redirectUri" +
                "&approval_prompt=force" +
                "&scope=read,activity:read" +
                "&state=$state"
    }

    suspend fun exchangeCodeForTokens(code: String): StravaTokenResponse {
        return authApi.exchangeToken(
            clientId = clientId,
            clientSecret = clientSecret,
            code = code,
            grantType = "authorization_code"
        )
    }

    suspend fun getActivities(member: Member): List<Activity> {
        val accessToken = stravaCredentialsService.getDecryptedAccessToken(member)
            ?: throw IllegalStateException("No Strava credentials found")

        // Set bearer token for this request
        activitiesApi.apiClient.setBearerToken(accessToken)

        return activitiesApi.getLoggedInAthleteActivities(
            before = null,
            after = null,
            page = 1,
            perPage = 30
        )
    }
}