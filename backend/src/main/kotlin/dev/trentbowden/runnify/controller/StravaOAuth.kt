package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.dto.AuthenticationResponse
import dev.trentbowden.runnify.entity.LoggedInMemberDto
import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.service.Strava.StravaService
import dev.trentbowden.runnify.service.auth.JwtService
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// DTO for Strava OAuth authorization response
@Schema(description = "The AuthUrl to pass to Strava for OAuth authorisation, with a state key for CSRF safety")
data class StravaAuthUrlDto(
    val authUrl: String,
    val state: String
)


@RestController
@CrossOrigin(origins = ["http://127.0.0.1:5173"])
class StravaOAuthController(private val stravaService: StravaService, private val jwtService: JwtService) {

    @GetMapping("/auth/strava/login")
    fun generateStravaAuthorisationUrl(): StravaAuthUrlDto {
        // State is a verifiable string for CSRF protection.
        // It ensures the auth response corresponds to a request initiated by us, and non a third party.
        val state = UUID.randomUUID().toString()
        val authUrl = stravaService.getAuthUrl(state)
        return StravaAuthUrlDto(authUrl.toURL().toString(), state)
    }

    @PostMapping("/auth/strava/callback")
    fun handleStravaCallback(@RequestParam code: String, @RequestParam state: String): AuthenticationResponse {
        val member = stravaService.getOrSetMemberFromAuthCode(code, state)
        val jwtToken = jwtService.generateToken(member)

        val userDto = LoggedInMemberDto(
            id = member.id!!,
            name = member.name,
            avatarUrl = member.avatarUrl
        )

        return AuthenticationResponse(
            token = jwtToken,
            user = userDto
        )

    }
}
