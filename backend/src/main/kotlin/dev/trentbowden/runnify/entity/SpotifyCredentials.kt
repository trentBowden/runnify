package dev.trentbowden.runnify.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.util.*

@Entity
@Table(name = "spotify_credentials")
data class SpotifyCredentials(
        @Id
        @GeneratedValue
        @UuidGenerator
        val id: UUID? = null,

        @OneToOne
        @JoinColumn(name = "member_id", nullable = false)
        val member: Member,

        // Encrypted tokens - stored as encrypted strings
        @Column(name = "access_token", nullable = false, length = 1000)
        val encryptedAccessToken: String,

        @Column(name = "refresh_token", nullable = true, length = 1000)
        val encryptedRefreshToken: String?,

        @Column(name = "expires_at", nullable = false)
        val expiresAt: Instant,

        @Column(name = "created_at", nullable = false)
        val createdAt: Instant = Instant.now(),

        @Column(name = "updated_at", nullable = false)
        val updatedAt: Instant = Instant.now()
) {
        // Helper methods to work with decrypted tokens
        fun getDecryptedAccessToken(encryptionService: dev.trentbowden.runnify.service.auth.EncryptionService): String? {
                return encryptionService.decrypt(encryptedAccessToken)
        }

        fun getDecryptedRefreshToken(encryptionService: dev.trentbowden.runnify.service.auth.EncryptionService): String? {
                return encryptedRefreshToken?.let { encryptionService.decrypt(it) }
        }
}
