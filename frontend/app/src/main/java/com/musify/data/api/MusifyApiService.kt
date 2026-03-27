package com.musify.data.api

import com.musify.data.models.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface MusifyApiService {
    
    // ========== Authentication ==========
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
    
    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponse>
    
    @GET("api/auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<MessageResponse>
    
    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Body email: Map<String, String>): Response<MessageResponse>

    @POST("api/auth/verify-sms")
    suspend fun verifySMS(@Body request: Map<String, String>): Response<MessageResponse>

    @POST("api/auth/resend-sms")
    suspend fun resendVerificationSMS(@Body request: Map<String, String>): Response<MessageResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Response<MessageResponse>
    
    // ========== Songs ==========
    @GET("api/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<Song>>
    
    @GET("api/songs/{id}")
    suspend fun getSongDetails(@Path("id") songId: Int): Response<SongDetails>
    
    @POST("api/songs/{id}/favorite")
    suspend fun toggleFavorite(@Path("id") songId: Int): Response<Map<String, Boolean>>
    
    @GET("api/songs/stream/{id}")
    @Streaming
    suspend fun streamSong(
        @Path("id") songId: Int,
        @Query("quality") quality: String? = null,
        @Header("Range") range: String? = null
    ): Response<ResponseBody>
    
    @GET("api/songs/stream/{id}/url")
    suspend fun getStreamUrl(
        @Path("id") songId: Int,
        @Query("quality") quality: String? = null
    ): Response<Map<String, String>>
    
    @POST("api/songs/skip")
    suspend fun skipSong(@Body request: Map<String, Any>): Response<Map<String, Any>>
    
    // ========== Playlists ==========
    @GET("api/playlists")
    suspend fun getCurrentUserPlaylists(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<Playlist>>
    
    @GET("api/users/{id}/playlists")
    suspend fun getUserPlaylists(
        @Path("id") userId: Int,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<Playlist>>
    
    @GET("api/playlists/{id}")
    suspend fun getPlaylistDetails(@Path("id") playlistId: Int): Response<PlaylistDetails>
    
    @POST("api/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): Response<Playlist>
    
    @POST("api/playlists/{id}/songs")
    suspend fun addSongToPlaylist(
        @Path("id") playlistId: Int,
        @Body request: Map<String, Int>
    ): Response<Unit>
    
    @DELETE("api/playlists/{id}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("id") playlistId: Int,
        @Path("songId") songId: Int
    ): Response<Unit>
    
    @PUT("api/playlists/{id}")
    suspend fun updatePlaylist(
        @Path("id") playlistId: Int,
        @Body request: Map<String, Any>
    ): Response<Playlist>
    
    @DELETE("api/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") playlistId: Int): Response<Unit>
    
    @POST("api/playlists/{id}/follow")
    suspend fun followPlaylist(@Path("id") playlistId: Int): Response<Unit>
    
    @DELETE("api/playlists/{id}/follow")
    suspend fun unfollowPlaylist(@Path("id") playlistId: Int): Response<Unit>
    
    @GET("api/playlists/followed")
    suspend fun getFollowedPlaylists(): Response<List<Playlist>>
    
    // ========== Albums ==========
    @GET("api/albums")
    suspend fun getAlbums(
        @Query("sort") sort: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<Album>>
    
    @GET("api/albums/{id}")
    suspend fun getAlbumDetails(@Path("id") albumId: Int): Response<AlbumDetails>
    
    // ========== Artists ==========
    @GET("api/artists")
    suspend fun getArtists(
        @Query("sort") sort: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<Artist>>
    
    @GET("api/artists/{id}")
    suspend fun getArtistDetails(@Path("id") artistId: Int): Response<Artist>
    
    @GET("api/artists/{id}/songs")
    suspend fun getArtistSongs(
        @Path("id") artistId: Int,
        @Query("sort") sort: String = "popular",
        @Query("limit") limit: Int = 50
    ): Response<List<Song>>
    
    @GET("api/artists/{id}/albums")
    suspend fun getArtistAlbums(@Path("id") artistId: Int): Response<List<Album>>
    
    @POST("api/artists/{id}/follow")
    suspend fun followArtist(@Path("id") artistId: Int): Response<MessageResponse>
    
    @DELETE("api/artists/{id}/follow")
    suspend fun unfollowArtist(@Path("id") artistId: Int): Response<MessageResponse>
    
    // ========== Search ==========
    @POST("api/search")
    suspend fun search(@Body request: Map<String, Any>): Response<Map<String, Any>>
    
    @GET("api/search/autocomplete")
    suspend fun autocomplete(@Query("q") query: String): Response<Map<String, Any>>
    
    @GET("api/search/trending")
    suspend fun getTrending(
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 10
    ): Response<Map<String, Any>>
    
    // ========== User ==========
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<User>
    
    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") userId: Int): Response<UserProfileResponse>
    
    @PUT("api/users/me")
    suspend fun updateProfile(
        @Body updates: Map<String, Any>
    ): Response<User>
    
    @PUT("api/users/me/password")
    suspend fun changePassword(
        @Body request: Map<String, String>
    ): Response<MessageResponse>
    
    @DELETE("api/users/me")
    suspend fun deleteAccount(
        @Body request: Map<String, String>
    ): Response<MessageResponse>
    
    @GET("api/users/{id}/playlists")
    suspend fun getUserPublicPlaylists(
        @Path("id") userId: Int,
        @Query("public") public: Boolean = true
    ): Response<List<Playlist>>
    
    // ========== Library ==========
    @GET("api/users/{id}/liked-songs")
    suspend fun getLikedSongs(
        @Path("id") userId: Int,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<Song>>
    
    @GET("api/users/{id}/following")
    suspend fun getFollowedArtists(@Path("id") userId: Int): Response<List<Artist>>
    
    @GET("api/users/{id}/followers")
    suspend fun getUserFollowers(@Path("id") userId: Int): Response<List<User>>
    
    @GET("api/users/{id}/following/users")
    suspend fun getUserFollowing(@Path("id") userId: Int): Response<List<User>>
    
    @POST("api/users/{id}/follow")
    suspend fun followUser(@Path("id") userId: Int): Response<Unit>
    
    @DELETE("api/users/{id}/follow")
    suspend fun unfollowUser(@Path("id") userId: Int): Response<Unit>
    
    @GET("api/users/{id}/history")
    suspend fun getListeningHistory(
        @Path("id") userId: Int,
        @Query("limit") limit: Int = 50
    ): Response<List<Song>>
    
    // ========== Recommendations ==========
    @GET("api/recommendations")
    suspend fun getRecommendations(
        @Query("limit") limit: Int = 20,
        @Query("seed_songs") seedSongs: String? = null,
        @Query("seed_artists") seedArtists: String? = null
    ): Response<List<Song>>
    
    @GET("api/recommendations/daily-mix")
    suspend fun getDailyMixes(): Response<List<Playlist>>
    
    // ========== Queue ==========
    @GET("api/queue")
    suspend fun getQueue(): Response<Map<String, Any>>
    
    @POST("api/queue/add")
    suspend fun addToQueue(@Body request: Map<String, Int>): Response<MessageResponse>
    
    @DELETE("api/queue")
    suspend fun clearQueue(): Response<MessageResponse>
    
    @PUT("api/queue/reorder")
    suspend fun reorderQueue(@Body request: Map<String, Any>): Response<MessageResponse>
    
    // ========== Upload ==========
    @Multipart
    @POST("upload/song")
    suspend fun uploadSong(
        @Part audioFile: MultipartBody.Part,
        @Query("artistId") artistId: Int,
        @Query("genre") genre: String? = null
    ): Response<UploadSongResponse>
    
    @Multipart
    @POST("upload/cover/song/{id}")
    suspend fun uploadSongCover(
        @Path("id") songId: Int,
        @Part imageFile: MultipartBody.Part
    ): Response<Map<String, String>>

    // ========== Subscription ==========
    @POST("api/subscriptions")
    suspend fun subscribe(@Body request: Map<String, String>): Response<Map<String, Any>>

    @GET("api/subscriptions/current")
    suspend fun getCurrentSubscription(): Response<Map<String, Any>>
}