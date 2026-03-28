package com.musify.ui.screens.player

import com.musify.player.MusicPlayerManager
import com.musify.player.PlayerState
import com.musify.player.RepeatMode
import com.musify.player.SongInfo
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private lateinit var viewModel: PlayerViewModel
    private val musicPlayerManager = mockk<MusicPlayerManager>(relaxed = true)
    private val playerStateFlow = MutableStateFlow(PlayerState())

    private val testSong = SongInfo(
        id = 1,
        title = "Test Song",
        artistName = "Test Artist",
        coverArt = "https://example.com/cover.jpg",
        streamUrl = "https://example.com/stream/1",
        duration = 200
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { musicPlayerManager.playerState } returns playerStateFlow
        viewModel = PlayerViewModel(musicPlayerManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `play delegates to MusicPlayerManager`() {
        viewModel.play(testSong)

        verify { musicPlayerManager.play(testSong) }
    }

    @Test
    fun `pause delegates to MusicPlayerManager`() {
        viewModel.pause()

        verify { musicPlayerManager.pause() }
    }

    @Test
    fun `resume delegates to MusicPlayerManager`() {
        viewModel.resume()

        verify { musicPlayerManager.resume() }
    }

    @Test
    fun `next delegates to MusicPlayerManager`() {
        viewModel.next()

        verify { musicPlayerManager.next() }
    }

    @Test
    fun `previous delegates to MusicPlayerManager`() {
        viewModel.previous()

        verify { musicPlayerManager.previous() }
    }

    @Test
    fun `seekTo delegates to MusicPlayerManager`() {
        viewModel.seekTo(5000L)

        verify { musicPlayerManager.seekTo(5000L) }
    }

    @Test
    fun `toggleShuffle delegates to MusicPlayerManager`() {
        viewModel.toggleShuffle()

        verify { musicPlayerManager.toggleShuffle() }
    }

    @Test
    fun `cycleRepeatMode delegates to MusicPlayerManager`() {
        viewModel.cycleRepeatMode()

        verify { musicPlayerManager.cycleRepeatMode() }
    }

    @Test
    fun `togglePlayPause calls pause when playing`() {
        playerStateFlow.value = PlayerState(isPlaying = true)

        viewModel.togglePlayPause()

        verify { musicPlayerManager.pause() }
    }

    @Test
    fun `togglePlayPause calls resume when paused`() {
        playerStateFlow.value = PlayerState(isPlaying = false)

        viewModel.togglePlayPause()

        verify { musicPlayerManager.resume() }
    }

    @Test
    fun `playerState reflects MusicPlayerManager state`() {
        val song = testSong
        playerStateFlow.value = PlayerState(
            currentSong = song,
            isPlaying = true,
            currentPosition = 5000L,
            duration = 200000L,
            queue = listOf(song),
            currentIndex = 0,
            shuffleEnabled = true,
            repeatMode = RepeatMode.ALL
        )

        val state = viewModel.playerState.value
        assertEquals(song, state.currentSong)
        assertTrue(state.isPlaying)
        assertEquals(5000L, state.currentPosition)
        assertEquals(200000L, state.duration)
        assertEquals(1, state.queue.size)
        assertEquals(0, state.currentIndex)
        assertTrue(state.shuffleEnabled)
        assertEquals(RepeatMode.ALL, state.repeatMode)
    }

    @Test
    fun `playQueue delegates to MusicPlayerManager with correct parameters`() {
        val songs = listOf(testSong, testSong.copy(id = 2, title = "Song 2"))

        viewModel.playQueue(songs, 1)

        verify { musicPlayerManager.playQueue(songs, 1) }
    }

    @Test
    fun `addToQueue delegates to MusicPlayerManager`() {
        viewModel.addToQueue(testSong)

        verify { musicPlayerManager.addToQueue(testSong) }
    }

    @Test
    fun `initial player state is default`() {
        val state = viewModel.playerState.value
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPosition)
        assertEquals(0L, state.duration)
        assertTrue(state.queue.isEmpty())
        assertFalse(state.shuffleEnabled)
        assertEquals(RepeatMode.OFF, state.repeatMode)
    }
}
