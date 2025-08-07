package dev.trentbowden.runnify.service.auth

import dev.trentbowden.runnify.entity.Member
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration-hours:24}")
    private var expirationHours: Long = 24

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun init() {
        if (jwtSecret.length < 32) {
            throw IllegalStateException("JWT secret must be at least 32 characters long")
        }
        secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateToken(member: Member): String {
        val now = Instant.now()
        val expiration = now.plus(expirationHours, ChronoUnit.HOURS)

        return Jwts.builder()
            .subject(member.id.toString())
            .claim("oauthId", member.oauthId)
            .claim("oauthProvider", member.oauthProvider.toString())
            .claim("name", member.name)
            .claim("email", member.email)
            .claim("avatarUrl", member.avatarUrl)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null // Token is invalid or expired
        }
    }

    fun extractMemberId(token: String): UUID? {
        return validateToken(token)?.subject?.let { UUID.fromString(it) }
    }

    fun isTokenExpired(token: String): Boolean {
        val claims = validateToken(token) ?: return true
        return claims.expiration.before(Date())
    }
}
