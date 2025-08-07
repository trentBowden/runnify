package dev.trentbowden.runnify.repository

import dev.trentbowden.runnify.entity.OAuthState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OAuthStateRepository : JpaRepository<OAuthState, String> {
    // Spring data JPA fills in the repository with crud methods for us.
}