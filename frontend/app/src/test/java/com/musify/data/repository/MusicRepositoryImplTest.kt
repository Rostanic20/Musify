package com.musify.data.repository

import com.musify.data.api.MusifyApiService
import com.musify.data.models.Album
import com.musify.data.models.AlbumDetails
import com.musify.data.models.AlbumType
import com.musify.data.models.Song
import com.musify.data.models.User
import com.musify.domain.exception.NetworkException
import com.musify.domain.exception.ServerException
import com.musify.domain.exception.SongNotFoundException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MusicRepositoryImplTest {

    private lateinit var repository: MusicRepositoryImpl
    private val apiService = mockk<MusifyApiService>()

    private val dataSong = Song(
        id = 1,
        title = "Test Song",
        artistId = 10,
        artistName = "Test Artist",
        albumId = null,
        albumTitle = null,
        duration = 200,
        filePath = "/audio/test.mp3",
        streamUrl = "https://example.com/stream/1",
        coverArt = "https://example.com/cover.jpg",
        genre = "Pop",
        playCount = 100,
        createdAt = "2024-01-01T00:00:00"
    )

    private val dataAlbum = Album(
        id = 1,
        title = "Test Album",
        artistId = 10,
        artistName = "Test Artist",
        coverArt = "https://example.com/album.jpg",
        releaseDate = "2024-01-15",
        trackCount = 10,
        totalDuration = 3000,
        genre = "Pop",
        type = AlbumType.ALBUM
    )

    private val dataUser = User(
        id = 1,
        email = "test@example.com",
        username = "testuser",
        displayName = "Test User",
        createdAt = "2024-01-01T00:00:00",
        updatedAt = "2024-01-01T00:00:00"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = MusicRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSongs returns mapped domain songs on success`() = runTest {
        coEvery { apiService.getSongs(any(), any()) } returns Response.success(listOf(dataSong))

        val result = repository.getSongs()

        assertTrue(result.isSuccess)
        val songs = result.getOrNull()!!
        assertEquals(1, songs.size)
        assertEquals("Test Song", songs[0].title)
        assertEquals("Test Artist", songs[0].artist.name)
        assertEquals(200, songs[0].durationSeconds)
    }

    @Test
    fun `getSongs returns failure on API error`() = runTest {
        coEvery { apiService.getSongs(any(), any()) } returns
            Response.error(500, "Server Error".toResponseBody())

        val result = repository.getSongs()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerException)
    }

    @Test
    fun `getSongs returns failure on network error`() = runTest {
        coEvery { apiService.getSongs(any(), any()) } throws IOException("No internet")

        val result = repository.getSongs()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException)
    }

    @Test
    fun `getSongs returns empty list when body is null`() = runTest {
        coEvery { apiService.getSongs(any(), any()) } returns Response.success(null)

        val result = repository.getSongs()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getRecentlyPlayed calls listening history API`() = runTest {
        coEvery { apiService.getCurrentUser() } returns Response.success(dataUser)
        coEvery { apiService.getListeningHistory(1, 10) } returns Response.success(listOf(dataSong))

        val result = repository.getRecentlyPlayed()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `getRecentlyPlayed falls back to getSongs on user fetch failure`() = runTest {
        coEvery { apiService.getCurrentUser() } returns
            Response.error(401, "Unauthorized".toResponseBody())
        coEvery { apiService.getSongs(limit = 10) } returns Response.success(listOf(dataSong))

        val result = repository.getRecentlyPlayed()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `searchSongs returns parsed results on success`() = runTest {
        val searchBody = mapOf<String, Any>(
            "songs" to listOf(
                mapOf(
                    "id" to 1.0,
                    "title" to "Found Song",
                    "artistName" to "Found Artist",
                    "artistId" to 10.0,
                    "duration" to 180.0,
                    "coverArt" to "https://example.com/cover.jpg",
                    "genre" to "Rock",
                    "playCount" to 50.0
                )
            )
        )
        coEvery { apiService.search(any()) } returns Response.success(searchBody)

        val result = repository.searchSongs("test query")

        assertTrue(result.isSuccess)
        val songs = result.getOrNull()!!
        assertEquals(1, songs.size)
        assertEquals("Found Song", songs[0].title)
        assertEquals("Found Artist", songs[0].artist.name)
    }

    @Test
    fun `searchSongs returns failure on API error`() = runTest {
        coEvery { apiService.search(any()) } returns
            Response.error(500, "Error".toResponseBody())

        val result = repository.searchSongs("test")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerException)
    }

    @Test
    fun `toggleFavorite returns correct state on success`() = runTest {
        coEvery { apiService.toggleFavorite(1) } returns
            Response.success(mapOf("isFavorite" to true))

        val result = repository.toggleFavorite(1)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!)
    }

    @Test
    fun `toggleFavorite returns false when unfavoriting`() = runTest {
        coEvery { apiService.toggleFavorite(1) } returns
            Response.success(mapOf("isFavorite" to false))

        val result = repository.toggleFavorite(1)

        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!)
    }

    @Test
    fun `toggleFavorite returns failure on API error`() = runTest {
        coEvery { apiService.toggleFavorite(any()) } returns
            Response.error(500, "Error".toResponseBody())

        val result = repository.toggleFavorite(1)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServerException)
    }

    @Test
    fun `getRecommendations returns mapped songs on success`() = runTest {
        coEvery { apiService.getRecommendations(any(), any(), any()) } returns
            Response.success(listOf(dataSong))

        val result = repository.getRecommendations()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `getPopularAlbums returns mapped albums on success`() = runTest {
        coEvery { apiService.getAlbums(sort = "popular", limit = any(), offset = any()) } returns
            Response.success(listOf(dataAlbum))

        val result = repository.getPopularAlbums()

        assertTrue(result.isSuccess)
        val albums = result.getOrNull()!!
        assertEquals(1, albums.size)
        assertEquals("Test Album", albums[0].title)
        assertEquals(2024, albums[0].releaseYear)
    }

    @Test
    fun `getNewReleases returns mapped albums on success`() = runTest {
        coEvery { apiService.getAlbums(sort = "newest", limit = any(), offset = any()) } returns
            Response.success(listOf(dataAlbum))

        val result = repository.getNewReleases()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `getSongById returns 404 as SongNotFoundException`() = runTest {
        coEvery { apiService.getSongDetails(999) } returns
            Response.error(404, "Not Found".toResponseBody())

        val result = repository.getSongById(999)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SongNotFoundException)
    }

    @Test
    fun `getArtistSongs returns mapped songs on success`() = runTest {
        coEvery { apiService.getArtistSongs(any(), any(), any()) } returns
            Response.success(listOf(dataSong))

        val result = repository.getArtistSongs(10)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }
}
