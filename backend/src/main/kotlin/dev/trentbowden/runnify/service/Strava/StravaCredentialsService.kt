package dev.trentbowden.runnify.service.Strava

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.StravaCredentials
import dev.trentbowden.runnify.repository.StravaCredentialsRepository
import dev.trentbowden.runnify.service.auth.EncryptionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class StravaCredentialsService(
    private val stravaCredentialsRepository: StravaCredentialsRepository,
    private val encryptionService: EncryptionService
) {

    @Value("\${strava.client-id}")
    private lateinit var clientId: String

    @Value("\${strava.client-secret}")
    private lateinit var clientSecret: String


    // Performs an upsert for credentials for the member.
    fun saveCredentials(
        member: Member,
        accessToken: String,
        refreshToken: String?,
        expiresIn: Int
    ): StravaCredentials {


        val expiresAt = Instant.now().plus(expiresIn.toLong(), ChronoUnit.SECONDS)

        // Find existing or create new
        val credentials = stravaCredentialsRepository.findByMember(member) ?: StravaCredentials(
            member = member,
            encryptedAccessToken = encryptionService.encrypt(accessToken)!!,
            encryptedRefreshToken = refreshToken?.let { encryptionService.encrypt(it) },
            expiresAt = Instant.now().plus(expiresIn.toLong(), ChronoUnit.SECONDS),

            // MYTODO Unsure what to put here..
            athleteId = "unknownAthleteId",

        )

        // Update existing credentials
        val updatedCredentials = credentials.copy(
            encryptedAccessToken = encryptionService.encrypt(accessToken)!!,
            encryptedRefreshToken = refreshToken?.let { encryptionService.encrypt(it) } ?: credentials.encryptedRefreshToken,
            expiresAt = expiresAt
        )


        return stravaCredentialsRepository.save(updatedCredentials)
    }

    fun getCredentials(member: Member): StravaCredentials? {
        return stravaCredentialsRepository.findByMember(member)
    }

    fun getDecryptedAccessToken(member: Member): String? {
        val credentials = getCredentials(member) ?: return null

        // Check if token is expired
        if (credentials.expiresAt.isBefore(Instant.now())) {
            // Try to refresh the token
            return refreshAccessToken(member, credentials)
        }

        return credentials.getDecryptedAccessToken(encryptionService)
    }

    private fun refreshAccessToken(member: Member, credentials: StravaCredentials): String? {
        val refreshToken = credentials.getDecryptedRefreshToken(encryptionService) ?: return null

        try {
            val stravaApi = StravaApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build()

            val authorizationCodeRefreshRequest = stravaApi.authorizationCodeRefresh()
                .refresh_token(refreshToken)
                .build()

            val authorizationCodeCredentials = authorizationCodeRefreshRequest.execute()

            // Save the new access token (refresh token usually stays the same)
            val newRefreshToken = authorizationCodeCredentials.refreshToken ?: refreshToken
            saveCredentials(
                member = member,
                accessToken = authorizationCodeCredentials.accessToken,
                refreshToken = newRefreshToken,
                expiresIn = authorizationCodeCredentials.expiresIn
            )

            return authorizationCodeCredentials.accessToken

        } catch (e: IOException) {
            println("Failed to refresh access token (IO): ${e.message}")
            return null
        } catch (e: StravaWebApiException) {
            println("Failed to refresh access token (API): ${e.message}")
            // If refresh fails, the user needs to re-authenticate
            return null
        }

    }

    fun deleteCredentials(member: Member) {
        stravaCredentialsRepository.deleteByMember(member)
    }
}