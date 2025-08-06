// Hello world controller
package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.entity.PageView
import dev.trentbowden.runnify.service.PageViewService
import dev.trentbowden.runnify.service.Spotify.Playlist
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:5173"])
class UserPlaylistsController() {
    @GetMapping("/playlists")
    fun getPlaylists(): List<Playlist> {
        return listOf(
            Playlist("123", "My Playlist 1", tracks = listOf(), "https://open.spotify.com/playlist/123"),
        );
    }
}
