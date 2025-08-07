package dev.trentbowden.runnify.repository

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.OAuthProvider
import dev.trentbowden.runnify.entity.OAuthState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MemberRepository : JpaRepository<Member, UUID> {
    fun findByOauthIdAndOauthProvider(oauthId: String, oauthProvider: OAuthProvider): Member?

}