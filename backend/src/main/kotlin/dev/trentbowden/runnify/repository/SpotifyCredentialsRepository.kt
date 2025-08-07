package dev.trentbowden.runnify.repository

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.OAuthProvider
import dev.trentbowden.runnify.entity.OAuthState
import dev.trentbowden.runnify.entity.SpotifyCredentials
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SpotifyCredentialsRepository : JpaRepository<SpotifyCredentials, UUID> {
    fun findByMember(member: Member): SpotifyCredentials?
    fun deleteByMember(member: Member)

}