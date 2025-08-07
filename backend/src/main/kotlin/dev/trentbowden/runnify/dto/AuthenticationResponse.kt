package dev.trentbowden.runnify.dto

import dev.trentbowden.runnify.entity.LoggedInMemberDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing user information and JWT token")
data class AuthenticationResponse(
    @Schema(description = "JWT token for authenticated requests")
    val token: String,

    @Schema(description = "Authenticated user information")
    val user: LoggedInMemberDto
)
