package dev.trentbowden.runnify.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "oauth_state")
data class OAuthState(
        @Id
        @Column(name = "state_key", unique = true, nullable = false, length = 255)
        val stateKey: String,

        @Column(name = "created_at", nullable = false)
        val createdAt: Instant = Instant.now()
)