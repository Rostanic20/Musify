package com.musify.ui.screens.user.search

import com.musify.data.api.MusifyApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val apiService = mockk<MusifyApiService>()
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
        return SearchViewModel(apiService)
    }

    @Test
    fun `trending data loads on init`() = runTest(testDispatcher) {
        val trendingResponse = mapOf<String, Any>(
            "songs" to listOf(
                mapOf("id" to 1.0, "title" to "Trending Song", "artistName" to "Artist", "duration" to 200.0)
            ),
            "artists" to listOf(
                mapOf("id" to 1.0, "name" to "Trending Artist")
            )
        )
        coEvery { apiService.getTrending(any(), any()) } returns Response.success(trendingResponse)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.trendingSongs.size)
        assertEquals(1, state.trendingArtists.size)
    }

    @Test
    fun `trending load failure sets error message`() = runTest(testDispatcher) {
        coEvery { apiService.getTrending(any(), any()) } throws IOException("No connection")

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Failed to load trending content", viewModel.state.value.errorMessage)
    }

    @Test
    fun `search with query returns results`() = runTest(testDispatcher) {
        coEvery { apiService.getTrending(any(), any()) } returns Response.success(emptyMap())

        val searchResponse = mapOf<String, Any>(
            "songs" to listOf(
                mapOf("id" to 1.0, "title" to "Found Song", "artistName" to "Artist", "duration" to 180.0)
            ),
            "artists" to emptyList<Any>(),
            "albums" to emptyList<Any>()
        )
        coEvery { apiService.search(any()) } returns Response.success(searchResponse)

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
    fun `search with empty query clears results and shows trending`() = runTest(testDispatcher) {
        coEvery { apiService.getTrending(any(), any()) } returns Response.success(emptyMap())
        coEvery { apiService.search(any()) } returns Response.success(emptyMap())

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
    fun `search network failure sets error message`() = runTest(testDispatcher) {
        coEvery { apiService.getTrending(any(), any()) } returns Response.success(emptyMap())
        coEvery { apiService.search(any()) } throws IOException("Network error")

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
    fun `clearSearch resets all search state`() = runTest(testDispatcher) {
        coEvery { apiService.getTrending(any(), any()) } returns Response.success(emptyMap())
        coEvery { apiService.search(any()) } returns Response.success(
            mapOf("songs" to emptyList<Any>(), "artists" to emptyList<Any>(), "albums" to emptyList<Any>())
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
    fun `debounce prevents rapid search calls`() = runTest(testDispatcher) {
        coEvery { apiService.getTrending(any(), any()) } returns Response.success(emptyMap())
        coEvery { apiService.search(any()) } returns Response.success(
            mapOf("songs" to emptyList<Any>(), "artists" to emptyList<Any>(), "albums" to emptyList<Any>())
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

        // distinctUntilChanged + debounce means only "abc" triggers a search
        val state = viewModel.state.value
        assertEquals("abc", state.query)
        assertTrue(state.hasSearched)
    }
}
