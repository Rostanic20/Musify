package com.musify.ui.screens.user.search

import com.musify.data.models.Song
import com.musify.data.models.Artist
import com.musify.domain.repository.MusicRepository
import com.musify.domain.repository.RawSearchResults
import com.musify.domain.repository.RawTrendingResults
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val musicRepository = mockk<MusicRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(musicRepository)
    }

    @Test
    fun trendingDataLoadsOnInit() = runTest(testDispatcher) {
        val trendingSong = mockk<Song>(relaxed = true)
        val trendingArtist = mockk<Artist>(relaxed = true)
        coEvery { musicRepository.getTrendingRaw(any()) } returns Result.success(
            RawTrendingResults(songs = listOf(trendingSong), artists = listOf(trendingArtist))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.trendingSongs.size)
        assertEquals(1, state.trendingArtists.size)
    }

    @Test
    fun trendingLoadFailureSetsErrorMessage() = runTest(testDispatcher) {
        coEvery { musicRepository.getTrendingRaw(any()) } returns Result.failure(IOException("No connection"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Failed to load trending content", viewModel.state.value.errorMessage)
    }

    @Test
    fun searchWithQueryReturnsResults() = runTest(testDispatcher) {
        coEvery { musicRepository.getTrendingRaw(any()) } returns Result.success(
            RawTrendingResults(songs = emptyList(), artists = emptyList())
        )
        val foundSong = mockk<Song>(relaxed = true)
        coEvery { musicRepository.searchRaw(any()) } returns Result.success(
            RawSearchResults(songs = listOf(foundSong), artists = emptyList(), albums = emptyList())
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("test query")
        advanceTimeBy(350)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.songs.size)
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
    }

    @Test
    fun searchWithEmptyQueryClearsResultsAndShowsTrending() = runTest(testDispatcher) {
        coEvery { musicRepository.getTrendingRaw(any()) } returns Result.success(
            RawTrendingResults(songs = emptyList(), artists = emptyList())
        )
        coEvery { musicRepository.searchRaw(any()) } returns Result.success(
            RawSearchResults(songs = emptyList(), artists = emptyList(), albums = emptyList())
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("some query")
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.onQueryChanged("")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.songs.isEmpty())
        assertTrue(state.artists.isEmpty())
        assertTrue(state.albums.isEmpty())
        assertFalse(state.hasSearched)
        assertNull(state.errorMessage)
    }

    @Test
    fun searchNetworkFailureSetsErrorMessage() = runTest(testDispatcher) {
        coEvery { musicRepository.getTrendingRaw(any()) } returns Result.success(
            RawTrendingResults(songs = emptyList(), artists = emptyList())
        )
        coEvery { musicRepository.searchRaw(any()) } returns Result.failure(IOException("Network error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("failing query")
        advanceTimeBy(350)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Network error. Please try again.", state.errorMessage)
        assertTrue(state.hasSearched)
    }

    @Test
    fun clearSearchResetsAllSearchState() = runTest(testDispatcher) {
        coEvery { musicRepository.getTrendingRaw(any()) } returns Result.success(
            RawTrendingResults(songs = emptyList(), artists = emptyList())
        )
        coEvery { musicRepository.searchRaw(any()) } returns Result.success(
            RawSearchResults(songs = emptyList(), artists = emptyList(), albums = emptyList())
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("test")
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.clearSearch()

        val state = viewModel.state.value
        assertEquals("", state.query)
        assertTrue(state.songs.isEmpty())
        assertTrue(state.artists.isEmpty())
        assertTrue(state.albums.isEmpty())
        assertTrue(state.autocompleteResults.isEmpty())
        assertFalse(state.hasSearched)
        assertNull(state.errorMessage)
    }

    @Test
    fun debouncePreventsRapidSearchCalls() = runTest(testDispatcher) {
        coEvery { musicRepository.getTrendingRaw(any()) } returns Result.success(
            RawTrendingResults(songs = emptyList(), artists = emptyList())
        )
        coEvery { musicRepository.searchRaw(any()) } returns Result.success(
            RawSearchResults(songs = emptyList(), artists = emptyList(), albums = emptyList())
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onQueryChanged("a")
        advanceTimeBy(100)
        viewModel.onQueryChanged("ab")
        advanceTimeBy(100)
        viewModel.onQueryChanged("abc")
        advanceTimeBy(350)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("abc", state.query)
        assertTrue(state.hasSearched)
    }
}
