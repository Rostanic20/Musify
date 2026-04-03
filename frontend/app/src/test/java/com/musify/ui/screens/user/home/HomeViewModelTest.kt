package com.musify.ui.screens.user.home

import com.musify.domain.entity.Album
import com.musify.domain.entity.Artist
import com.musify.domain.entity.Playlist
import com.musify.domain.entity.Song
import com.musify.domain.exception.NetworkException
import com.musify.domain.usecase.music.GetNewReleasesUseCase
import com.musify.domain.usecase.music.GetPopularAlbumsUseCase
import com.musify.domain.usecase.music.GetRecentlyPlayedUseCase
import com.musify.domain.usecase.music.GetRecommendationsUseCase
import com.musify.domain.usecase.playlist.GetUserPlaylistsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getRecentlyPlayedUseCase = mockk<GetRecentlyPlayedUseCase>()
    private val getRecommendationsUseCase = mockk<GetRecommendationsUseCase>()
    private val getPopularAlbumsUseCase = mockk<GetPopularAlbumsUseCase>()
    private val getNewReleasesUseCase = mockk<GetNewReleasesUseCase>()
    private val getUserPlaylistsUseCase = mockk<GetUserPlaylistsUseCase>()

    private val testArtist = Artist(
        id = 1,
        name = "Test Artist",
        bio = null,
        profileImageUrl = null,
        isVerified = false,
        monthlyListeners = 1000,
        followersCount = 500
    )

    private val testSong = Song(
        id = 1,
        title = "Test Song",
        artist = testArtist,
        album = null,
        durationSeconds = 200,
        coverArtUrl = null,
        genre = "Pop",
        playCount = 100,
        isFavorite = false
    )

    private val testAlbum = Album(
        id = 1,
        title = "Test Album",
        artist = testArtist,
        coverArtUrl = null,
        releaseYear = 2024,
        trackCount = 10,
        genre = "Pop"
    )

    private val testPlaylist = Playlist(
        id = 1,
        name = "Test Playlist",
        description = "A test playlist",
        ownerId = 1,
        ownerName = "testuser",
        isPublic = true,
        isCollaborative = false,
        coverImageUrl = null,
        songCount = 5,
        totalDurationSeconds = 1000,
        followersCount = 10
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            getRecentlyPlayedUseCase,
            getRecommendationsUseCase,
            getPopularAlbumsUseCase,
            getNewReleasesUseCase,
            getUserPlaylistsUseCase
        )
    }

    @Test
    fun initLoadsAllContentSuccessfully() = runTest {
        val songs = listOf(testSong)
        val albums = listOf(testAlbum)
        val playlists = listOf(testPlaylist)

        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.success(songs)
        coEvery { getRecommendationsUseCase(any()) } returns Result.success(songs)
        coEvery { getPopularAlbumsUseCase(any()) } returns Result.success(albums)
        coEvery { getNewReleasesUseCase(any()) } returns Result.success(albums)
        coEvery { getUserPlaylistsUseCase(any()) } returns Result.success(playlists)

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertEquals(1, state.recentlyPlayed.size)
        assertEquals(1, state.recommendations.size)
        assertEquals(1, state.popularAlbums.size)
        assertEquals(1, state.newReleases.size)
        assertEquals(1, state.userPlaylists.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun initHandlesAPIErrorsGracefullyWithEmptyLists() = runTest {
        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.failure(NetworkException("Network error"))
        coEvery { getRecommendationsUseCase(any()) } returns Result.failure(NetworkException("Network error"))
        coEvery { getPopularAlbumsUseCase(any()) } returns Result.success(emptyList())
        coEvery { getNewReleasesUseCase(any()) } returns Result.success(emptyList())
        coEvery { getUserPlaylistsUseCase(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertTrue(state.recentlyPlayed.isEmpty())
        assertTrue(state.recommendations.isEmpty())
        assertEquals("Failed to load some content", state.errorMessage)
    }

    @Test
    fun loadingStateIsFalseAfterInitCompletes() = runTest {
        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.success(emptyList())
        coEvery { getRecommendationsUseCase(any()) } returns Result.success(emptyList())
        coEvery { getPopularAlbumsUseCase(any()) } returns Result.success(emptyList())
        coEvery { getNewReleasesUseCase(any()) } returns Result.success(emptyList())
        coEvery { getUserPlaylistsUseCase(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun emptyDataResultsInEmptyListsNotNulls() = runTest {
        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.success(emptyList())
        coEvery { getRecommendationsUseCase(any()) } returns Result.success(emptyList())
        coEvery { getPopularAlbumsUseCase(any()) } returns Result.success(emptyList())
        coEvery { getNewReleasesUseCase(any()) } returns Result.success(emptyList())
        coEvery { getUserPlaylistsUseCase(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertTrue(state.recentlyPlayed.isEmpty())
        assertTrue(state.recommendations.isEmpty())
        assertTrue(state.popularAlbums.isEmpty())
        assertTrue(state.newReleases.isEmpty())
        assertTrue(state.userPlaylists.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun partialFailureSetsErrorMessageButStillLoadsSuccessfulData() = runTest {
        val songs = listOf(testSong)
        val albums = listOf(testAlbum)

        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.failure(NetworkException("fail"))
        coEvery { getRecommendationsUseCase(any()) } returns Result.success(songs)
        coEvery { getPopularAlbumsUseCase(any()) } returns Result.success(albums)
        coEvery { getNewReleasesUseCase(any()) } returns Result.success(albums)
        coEvery { getUserPlaylistsUseCase(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()
        val state = viewModel.state.value

        assertTrue(state.recentlyPlayed.isEmpty())
        assertEquals(1, state.recommendations.size)
        assertEquals(1, state.popularAlbums.size)
        assertEquals("Failed to load some content", state.errorMessage)
    }

    @Test
    fun refreshReloadsAllContent() = runTest {
        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.success(emptyList())
        coEvery { getRecommendationsUseCase(any()) } returns Result.success(emptyList())
        coEvery { getPopularAlbumsUseCase(any()) } returns Result.success(emptyList())
        coEvery { getNewReleasesUseCase(any()) } returns Result.success(emptyList())
        coEvery { getUserPlaylistsUseCase(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.recentlyPlayed.isEmpty())

        val songs = listOf(testSong)
        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.success(songs)
        coEvery { getRecommendationsUseCase(any()) } returns Result.success(songs)

        viewModel.refresh()

        assertEquals(1, viewModel.state.value.recentlyPlayed.size)
        assertEquals(1, viewModel.state.value.recommendations.size)
    }

    @Test
    fun noErrorMessageWhenAllRequestsSucceed() = runTest {
        coEvery { getRecentlyPlayedUseCase(any()) } returns Result.success(listOf(testSong))
        coEvery { getRecommendationsUseCase(any()) } returns Result.success(listOf(testSong))
        coEvery { getPopularAlbumsUseCase(any()) } returns Result.success(listOf(testAlbum))
        coEvery { getNewReleasesUseCase(any()) } returns Result.success(listOf(testAlbum))
        coEvery { getUserPlaylistsUseCase(any()) } returns Result.success(listOf(testPlaylist))

        val viewModel = createViewModel()

        assertNull(viewModel.state.value.errorMessage)
    }
}
