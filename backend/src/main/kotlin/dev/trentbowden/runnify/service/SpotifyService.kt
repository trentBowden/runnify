package dev.trentbowden.runnify.service

import se.michaelthelin.spotify.SpotifyApi
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException
import java.io.IOException
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest
import java.net.URI

@Service
class SpotifyClient  {
    // This will be initialised in init
    private lateinit var spotifyApi: SpotifyApi

    @Value("\${spotify.client-id}")
    private lateinit var clientId: String

    @Value("\${spotify.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${spotify.redirect-uri}")
    private lateinit var redirectUri: URI

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
                                  AuthorizationCodeUriRequest ): String? {
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
                              spotifyApi: SpotifyApi ): List<String>? {
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