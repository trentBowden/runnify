package dev.trentbowden.runnify.service.Spotify

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.SpotifyCredentials
import dev.trentbowden.runnify.repository.SpotifyCredentialsRepository
import dev.trentbowden.runnify.service.auth.EncryptionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class SpotifyCredentialsService(
    private val spotifyCredentialsRepository: SpotifyCredentialsRepository,
    private val encryptionService: EncryptionService
) {

    @Value("\${spotify.client-id}")
    private lateinit var clientId: String

    @Value("\${spotify.client-secret}")
    private lateinit var clientSecret: String


    // Performs an upsert for credentials for the member.
    fun saveCredentials(
        member: Member,
        accessToken: String,
        refreshToken: String?,
        expiresIn: Int
    ): SpotifyCredentials {


        val expiresAt = Instant.now().plus(expiresIn.toLong(), ChronoUnit.SECONDS)

        // Find existing or create new
        val credentials = spotifyCredentialsRepository.findByMember(member) ?: SpotifyCredentials(
            member = member,
            encryptedAccessToken = encryptionService.encrypt(accessToken)!!,
            encryptedRefreshToken = refreshToken?.let { encryptionService.encrypt(it) },
            expiresAt = expiresAt
        )

        // Update existing credentials
        val updatedCredentials = credentials.copy(
            encryptedAccessToken = encryptionService.encrypt(accessToken)!!,
            encryptedRefreshToken = refreshToken?.let { encryptionService.encrypt(it) } ?: credentials.encryptedRefreshToken,
            expiresAt = expiresAt
        )


        return spotifyCredentialsRepository.save(updatedCredentials)
    }

    fun getCredentials(member: Member): SpotifyCredentials? {
        return spotifyCredentialsRepository.findByMember(member)
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

    private fun refreshAccessToken(member: Member, credentials: SpotifyCredentials): String? {
        val refreshToken = credentials.getDecryptedRefreshToken(encryptionService) ?: return null

        try {
            val spotifyApi = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build()

            val authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh()
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
        } catch (e: SpotifyWebApiException) {
            println("Failed to refresh access token (API): ${e.message}")
            // If refresh fails, the user needs to re-authenticate
            return null
        }

    }

    fun deleteCredentials(member: Member) {
        spotifyCredentialsRepository.deleteByMember(member)
    }
}
