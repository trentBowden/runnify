package dev.trentbowden.runnify.service.Member

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A Runnify member.")
data class Member(
    @Schema(description = "The ID of the member.")
    val id: String,

    @Schema(description = "The name of the member.")
    val name: String,

    @Schema(description = "The URL of the member's avatar.")
    val avatarUrl: String? = null
)
