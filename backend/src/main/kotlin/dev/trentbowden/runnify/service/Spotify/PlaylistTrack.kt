package dev.trentbowden.runnify.service.Spotify

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A playlist of tracks.")
data class PlaylistTrack(

    @Schema(description = "The Spotify ID for the playlist.")
    val id: String,

    @Schema(description = "The name of the playlist.")
    val name: String,

    @Schema(description = "URL for previewing the track")
    val previewUrl: String? = null,

    @Schema(description = "The track URI")
    val uri: String? = null,

    @Schema(description = "Popularity of this track")
    val popularity: Int? = null,
)
