package com.musify.ui.navigation

object Routes {
    // Top-level routes
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val MAIN = "main"
    
    // Auth routes
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val EMAIL_VERIFICATION = "email_verification/{email}"
    
    // Main app routes
    const val HOME = "home"
    const val BROWSE = "browse"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val PROFILE = "profile"
    
    // Detail routes
    const val SONG_DETAIL = "song/{songId}"
    const val ALBUM_DETAIL = "album/{albumId}"
    const val ARTIST_DETAIL = "artist/{artistId}"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    const val USER_PROFILE = "user/{userId}"
    
    // Player routes
    const val NOW_PLAYING = "now_playing"
    const val QUEUE = "queue"
    
    // Settings
    const val SETTINGS = "settings"
    const val SUBSCRIPTION = "subscription"
    
    // Helper functions for navigation with arguments
    fun songDetail(songId: Int) = "song/$songId"
    fun albumDetail(albumId: Int) = "album/$albumId"
    fun artistDetail(artistId: Int) = "artist/$artistId"
    fun playlistDetail(playlistId: Int) = "playlist/$playlistId"
    fun userProfile(userId: Int) = "user/$userId"
    fun emailVerification(email: String) = "email_verification/$email"
}