package dev.trentbowden.runnify.service.Spotify

import dev.trentbowden.runnify.entity.Member
import dev.trentbowden.runnify.entity.OAuthProvider
import dev.trentbowden.runnify.entity.OAuthState
import dev.trentbowden.runnify.entity.Playlist
import dev.trentbowden.runnify.repository.MemberRepository
import dev.trentbowden.runnify.repository.OAuthStateRepository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import java.io.IOException
import java.net.URI
import java.time.Instant

@Service
class SpotifyService(
    private val oAuthStateRepository: OAuthStateRepository,
    private val memberRepository: MemberRepository,
    private val spotifyCredentialsService: SpotifyCredentialsService
)  {
    // This will be initialised in init
    private lateinit var spotifyApi: SpotifyApi

    @Value("\${spotify.client-id}")
    private lateinit var clientId: String

    @Value("\${spotify.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${spotify.redirect-uri}")
    private lateinit var redirectUri: URI

    @Value("\${spotify.public-playlist-id}")
    private lateinit var publicPlaylistId: String

    @Value("\${spotify.private-playlist-id}")
    private lateinit var privatePlaylistId: String

    private lateinit var accessToken: String

    @PostConstruct
    fun init() {

        if (clientId.isBlank()) {
            throw IllegalStateException("client id is empty")
        }
        if (clientSecret.isBlank()) {
            throw IllegalStateException("client secret is empty")
        }
        if (redirectUri.toString().isBlank()) {
            throw IllegalStateException("redirect uri is empty")
        }

        this.spotifyApi = SpotifyApi.builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectUri(redirectUri)
            .build()

        val clientCredentialsRequest = spotifyApi.clientCredentials().build()
        val clientCredentials = clientCredentialsRequest.execute()
        spotifyApi.accessToken = clientCredentials.accessToken

        println("Access Token: ${spotifyApi.accessToken}")
        println("Expires in: ${clientCredentials.expiresIn}s")

        accessToken = clientCredentials.accessToken
    }

    fun getAuthUrl(state: String): URI {

        // Save state key in the database
        saveNewStateKey(state)

        println("State Key: $state");

        return spotifyApi.authorizationCodeUri()
            .scope("user-read-private user-read-email playlist-read-private")
            .state(state)
            .build()
            .execute()
    }

    fun getOrSetMemberFromAuthCode(code: String, stateKey: String): Member {
        val validState = findValidState(stateKey)?: throw IllegalStateException("State key is invalid or timed out. Please try again.")

        // Step 1: Exchange code for Spotify Access TOkens
        val tokenCredentials = exchangeCodeForTokens(code)

        // Step 2: Fetch user profile from Spotify
        val spotifyUserProfile = fetchSpotifyUserProfile(tokenCredentials.accessToken)

        // Step 3: Create or update the member in the database
        return createOrUpdateMember(spotifyUserProfile, tokenCredentials)
    }

    /**
     * Communicate with Spotify to swap an auth code for access tokens (and refresh tokens)
     */
    private fun exchangeCodeForTokens(code: String): SpotifyTokenCredentials {
        try {
            val authorizationCodeRequest = spotifyApi.authorizationCode(code).build()
            val authorizationCodeCredentials = authorizationCodeRequest.execute()

            return SpotifyTokenCredentials(
                accessToken = authorizationCodeCredentials.accessToken,
                refreshToken = authorizationCodeCredentials.refreshToken,
                expiresIn = authorizationCodeCredentials.expiresIn
            )
        } catch (e: IOException) {
            throw RuntimeException("Failed to exchange code for tokens (IO): ${e.message}", e)
        } catch (e: SpotifyWebApiException) {
            throw RuntimeException("Failed to exchange code for tokens (API): ${e.message}", e)
        }

    }

    /**
     * Fetch user-profile from Spotify using the access token
     */
    private fun fetchSpotifyUserProfile(accessToken: String): SpotifyUserProfile {
        try {
            // Create a temporary SpotifyApi instance with the user's access token
            val userSpotifyApi = SpotifyApi.builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setAccessToken(accessToken)
                .build()

            val userProfile = userSpotifyApi.currentUsersProfile.build().execute()

            return SpotifyUserProfile(
                spotifyId = userProfile.id,
                displayName = userProfile.displayName,
                email = userProfile.email,
                profileImageUrl = userProfile.images?.firstOrNull()?.url
            )
        } catch (e: IOException) {
            throw RuntimeException("Failed to fetch user profile (IO): ${e.message}", e)
        } catch (e: SpotifyWebApiException) {
            throw RuntimeException("Failed to fetch user profile (API): ${e.message}", e)
        }
    }

    /**
     * Create new member or update existing member in database
     */
    private fun createOrUpdateMember(
        spotifyUserProfile: SpotifyUserProfile,
        tokenCredentials: SpotifyTokenCredentials
    ): Member {
        // Check if member already exists
        val existingMember = memberRepository.findByOauthIdAndOauthProvider(
            spotifyUserProfile.spotifyId,
            OAuthProvider.SPOTIFY
        )

        val member = if (existingMember != null) {
            // Update existing member
            val updatedMember = existingMember.copy(
                name = spotifyUserProfile.displayName,
                email = spotifyUserProfile.email,
                avatarUrl = spotifyUserProfile.profileImageUrl
            )
            memberRepository.save(updatedMember)
        } else {

            println("Member not found. Creating new member.")
            println("User profile: $spotifyUserProfile")
            // Create new member
            val newMember = Member(
                oauthId = spotifyUserProfile.spotifyId,
                oauthProvider = OAuthProvider.SPOTIFY,
                name = spotifyUserProfile.displayName,
                email = spotifyUserProfile.email,
                avatarUrl = spotifyUserProfile.profileImageUrl
            )
            memberRepository.save(newMember)
        }

        // Securely store Spotify credentials
        spotifyCredentialsService.saveCredentials(
            member = member,
            accessToken = tokenCredentials.accessToken,
            refreshToken = tokenCredentials.refreshToken,
            expiresIn = tokenCredentials.expiresIn
        )

        return member
    }




    /**
     * OAuth State getters/setters
     */
    fun saveNewStateKey(stateKey: String): OAuthState {
        val state = OAuthState(stateKey)
        return oAuthStateRepository.save(state)
    }

    fun findValidState(stateKey: String): OAuthState? {
        val state = oAuthStateRepository.findById(stateKey).orElse(null)
        if (state == null) {
            return null
        }

        println("FOund valid state with key: $stateKey")

        // If the state is older than ten minutes old, remove it.
        if (state.createdAt.isBefore(Instant.now().minusSeconds(600))) {
            println("State is older than 10 minutes. Removing it.")
            oAuthStateRepository.deleteById(stateKey)
            return null
        }

        // If the state is valid (and within 10 mins), return and wipe it to stop replay attacks.
        oAuthStateRepository.deleteById(stateKey)
        println("State is valid. Deleting it and returning..")
        return state
    }


    /**
     * Data class to hold Spotify token credentials
     */
    data class SpotifyTokenCredentials(
        val accessToken: String,
        val refreshToken: String?,
        val expiresIn: Int
    )

    /**
     * Data class to hold Spotify user profile information
     */
    data class SpotifyUserProfile(
        val spotifyId: String,
        val displayName: String?,
        val email: String?,
        val profileImageUrl: String?
    )


    /**
     * Get user's Spotify playlists using their stored credentials
     */
    fun getUserPlaylists(member: Member): List<Playlist> {
        val userSpotifyApi = createUserSpotifyApi(member)

        return try {
            val currentUsersPlaylists = userSpotifyApi.listOfCurrentUsersPlaylists.build().execute()

            currentUsersPlaylists.items.map { spotifyPlaylist ->
                Playlist(
                    id = spotifyPlaylist.id,
                    name = spotifyPlaylist.name,
                    tracks = listOf(),
//                    url = spotifyPlaylist.externalUrls.get("spotify") ?: ""
                )
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to fetch user playlists (IO): ${e.message}", e)
        } catch (e: SpotifyWebApiException) {
            throw RuntimeException("Failed to fetch user playlists (API): ${e.message}", e)
        }
    }

    /**
     * Get a specific playlist by ID for the authenticated user
     */
    fun getUserPlaylistById(member: Member, playlistId: String): Playlist {
        val userSpotifyApi = createUserSpotifyApi(member)

        return try {
            val spotifyPlaylist = userSpotifyApi.getPlaylist(playlistId).build().execute()
            val playlistTracks = userSpotifyApi.getPlaylistsItems(playlistId).build().execute()

            val tracks = playlistTracks.items.map { playlistTrack ->
                // TODO fill this in for our DTO
            }

            Playlist(
                id = spotifyPlaylist.id,
                name = spotifyPlaylist.name,
                tracks = listOf(),
//                url = spotifyPlaylist.externalUrls.get("spotify") ?: ""
            )
        } catch (e: IOException) {
            throw RuntimeException("Failed to fetch playlist (IO): ${e.message}", e)
        } catch (e: SpotifyWebApiException) {
            throw RuntimeException("Failed to fetch playlist (API): ${e.message}", e)
        }
    }

    /**
     * Create a SpotifyApi instance configured with user's access token
     */
    private fun createUserSpotifyApi(member: Member): SpotifyApi {
        val accessToken = spotifyCredentialsService.getDecryptedAccessToken(member)
            ?: throw IllegalStateException("No valid Spotify credentials found for user. Please re-authenticate.")

        return SpotifyApi.builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setAccessToken(accessToken)
            .build()
    }







    ///////////
    ///////////
    ///////////
    ///////////
    ///////////
    ///////////
    /// Everything below is just messing around.
    /// Everything above is legitimately intentional.
    ///////////
    ///////////
    ///////////
    ///////////
    ///////////

    // Can't do this without an oAuth flow, sorry.
    fun getCurrentUserProfile(): String {
        return try {

            if (spotifyApi.accessToken.isNullOrBlank()) {
                println("getCurrentUserProfile failed because Access token is null or blank")
            }

            println("Access token about to be used for profile getting: ${spotifyApi.accessToken}")

            val userProfile = spotifyApi.currentUsersProfile.build().execute()
            "Logged in as ${userProfile.displayName}"
        } catch (e: IOException) {
            "Error getting current user profile (IO): ${e.message}"
        } catch (e: SpotifyWebApiException) {
            "Error getting current user profile (API): ${e.message}"
        }
    }

    fun getPrivatePlaylistTracks(): Array<PlaylistTrack> {
        try {
            val tracks = spotifyApi.getPlaylistsItems(privatePlaylistId).build().execute()
            val playlist = spotifyApi.getPlaylist(privatePlaylistId).build().execute()
            println(playlist.name.toString())
            println(playlist.description.toString())
            println(playlist.images.map { it.url }.toString())

            println("Audio feature::")
            val tracksSpecific = spotifyApi.getSeveralTracks(tracks.items[0].track.id).build().execute()
            println(tracksSpecific.map { it.name + " " + it.popularity }.toString())

//            val audioFeatures =
//                spotifyApi.getAudioFeaturesForSeveralTracks(tracks.items.joinToString(",", transform = { it.track.id })).build().execute()
//            audioFeatures.toString()

            println("Tracks in private playlist:")
            println(tracks.items.toString())

            return tracks.items;

        } catch (e: IOException) {
            throw IllegalStateException("Error getting tracks in public playlist (IO): ${e.message}")
        }
    }

    fun getTracksInPlaylist(playlistId: String): String {
        return try {
            val tracks = spotifyApi.getPlaylistsItems(playlistId).build().execute()
            tracks.items.joinToString("\n") { it.track.name }
        } catch (e: IOException) {
            "Error getting tracks in playlist (IO): ${e.message}"
        }
    }


    fun searchTracks(query: String): String {
        return try {
            val trackPaging = spotifyApi.searchTracks(query).build().execute()
            if (trackPaging.items.isNotEmpty()) {
                val firstTrack = trackPaging.items[0]
                "Found track: ${firstTrack.name} by ${firstTrack.artists[0].name}"
            } else {
                "No tracks found for '$query'"
            }
        } catch (e: IOException) {
            "Error searching tracks (IO): ${e.message}"
        } catch (e: SpotifyWebApiException) {
            "Error searching tracks (API): ${e.message}"
        }
    }


    fun authorizationCodeUriSync( authorizationCodeUriRequest:
                                  AuthorizationCodeUriRequest
    ): String? {
        try {
            return authorizationCodeUriRequest.execute().toString()
        }catch (e: IOException){
            println("error " + e.localizedMessage)

        }catch (e: SpotifyWebApiException){
            println("Spotify web exception: " + e.localizedMessage)
        }
        return null
    }

    fun authorizationCodeSync(authorizationCodeRequest: AuthorizationCodeRequest,
                              spotifyApi: SpotifyApi
    ): List<String>? {
        try {
            val authorizationCodeCredentials = authorizationCodeRequest.execute()
            spotifyApi.accessToken = authorizationCodeCredentials.accessToken
            spotifyApi.refreshToken = authorizationCodeCredentials.refreshToken
            return listOf<String>(spotifyApi.accessToken,spotifyApi.refreshToken)

        }catch (e: IOException){
            println("error " + e.localizedMessage)

        }catch (e: SpotifyWebApiException){
            println("Spotify web exception: " + e.localizedMessage)
        }
        return null
    }
}