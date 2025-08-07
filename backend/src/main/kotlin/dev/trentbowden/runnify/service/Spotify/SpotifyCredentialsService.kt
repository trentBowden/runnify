package dev.trentbowden.runnify.service.Spotify

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.SpotifyCredentials
import dev.trentbowden.runnify.repository.SpotifyCredentialsRepository
import dev.trentbowden.runnify.service.auth.EncryptionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
@Transactional
class SpotifyCredentialsService(
    private val spotifyCredentialsRepository: SpotifyCredentialsRepository,
    private val encryptionService: EncryptionService
) {

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

        return spotifyCredentialsRepository.save(credentials)
    }

    fun getCredentials(member: Member): SpotifyCredentials? {
        return spotifyCredentialsRepository.findByMember(member)
    }

    fun getDecryptedAccessToken(member: Member): String? {
        val credentials = getCredentials(member) ?: return null

        // Check if token is expired
        if (credentials.expiresAt.isBefore(Instant.now())) {
            return null // Should trigger refresh flow
        }

        return credentials.getDecryptedAccessToken(encryptionService)
    }

    fun deleteCredentials(member: Member) {
        spotifyCredentialsRepository.deleteByMember(member)
    }
}
