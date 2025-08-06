package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.entity.PageView
import dev.trentbowden.runnify.service.Member.Member
import dev.trentbowden.runnify.service.PageViewService
import dev.trentbowden.runnify.service.Spotify.Playlist
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:5173"])
class LoginController() {
    @PostMapping("/login")
    fun login(): Member {
        return Member(
            id = "1234",
            name = "Test User",
            avatarUrl = null,

        );
    }
}
