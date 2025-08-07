package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.Playlist
import dev.trentbowden.runnify.repository.MemberRepository
import dev.trentbowden.runnify.service.Spotify.SpotifyService
import dev.trentbowden.runnify.service.auth.JwtService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@CrossOrigin(origins = ["http://127.0.0.1:5173"])
class UserPlaylistsController(
    private val spotifyService: SpotifyService,
    private val jwtService: JwtService,
    private val memberRepository: MemberRepository
) {
    
    @GetMapping("/playlists")
    fun getPlaylists(@RequestHeader("Authorization") authHeader: String): List<Playlist> {
        val member = getAuthenticatedMember(authHeader)
        return spotifyService.getUserPlaylists(member)
    }

    @GetMapping("/playlists/{id}")
    fun getPlaylistById(
        @PathVariable id: String,
        @RequestHeader("Authorization") authHeader: String
    ): Playlist {
        val member = getAuthenticatedMember(authHeader)
        return spotifyService.getUserPlaylistById(member, id)
    }
    
    private fun getAuthenticatedMember(authHeader: String): Member {
        // Extract token from "Bearer {token}" format
        val token = authHeader.removePrefix("Bearer ").trim()
        
        // Extract member ID from JWT
        val memberId = jwtService.extractMemberId(token) 
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        
        // Find member in database
        return memberRepository.findById(memberId).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Member not found")
    }
}