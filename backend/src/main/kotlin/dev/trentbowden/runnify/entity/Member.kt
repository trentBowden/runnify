package dev.trentbowden.runnify.entity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

enum class OAuthProvider {
    SPOTIFY
}

@Schema(description = "An object representing the current member")
data class LoggedInMemberDto(
    val id: UUID,
    val name: String?,
    val avatarUrl: String? = null
)

@Entity
@Table(name = "member", uniqueConstraints = [
    UniqueConstraint(columnNames = ["oauth_id", "oauth_provider"])
])
@Schema(description = "A Runnify member.")
data class Member(
    @Id
    @GeneratedValue
    @UuidGenerator
    @Schema(description = "The internal ID of the member.")
    val id: UUID? = null, // Corrected type to UUID

    @Schema(description = "The OAuth ID of the member.")
    @Column(name = "oauth_id", nullable = false)
    val oauthId: String,

    @Schema(description = "The OAuth provider of the member", defaultValue = "SPOTIFY")
    @Column(name = "oauth_provider", nullable = false)
    @Enumerated(EnumType.STRING)
    val oauthProvider: OAuthProvider? = OAuthProvider.SPOTIFY,

    @Schema(description = "The display name of the member.")
    @Column(name = "name", nullable = true)
    val name: String?,

    @Schema(description = "The email address of the member.")
    @Column(name = "email", unique = true, nullable = true)
    val email: String? = null,

    @Schema(description = "The URL of the member's avatar.")
    @Column(name = "avatar_url", nullable = true, length = 1000)
    val avatarUrl: String? = null
)