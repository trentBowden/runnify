package dev.trentbowden.runnify.entity

import dev.trentbowden.runnify.entity.PlaylistTrack
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "A playlist of tracks.")
data class Playlist(
    @Schema(description = "The Spotify ID for the playlist.")
    val id: String,

    @Schema(description = "The name of the playlist.")
    val name: String,

    @Schema(description = "The tracks in the playlist.")
    val tracks: List<PlaylistTrack>,

    @Schema(description="The URL of the album cover for this playlist")
    val coverUrl: String? = null
)