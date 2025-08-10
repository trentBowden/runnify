package dev.trentbowden.runnify.repository

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.OAuthProvider
import dev.trentbowden.runnify.entity.OAuthState
import dev.trentbowden.runnify.entity.SpotifyCredentials
import dev.trentbowden.runnify.entity.StravaCredentials
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StravaCredentialsRepository : JpaRepository<StravaCredentials, UUID> {
    fun findByMember(member: Member): StravaCredentials?
    fun deleteByMember(member: Member)

}