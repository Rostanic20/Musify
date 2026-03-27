package com.musify.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
class MusicPlayerManager(
    private val context: Context
) {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().also { player ->
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _playerState.update {
                                it.copy(duration = player.duration.coerceAtLeast(0L))
                            }
                        }
                        Player.STATE_ENDED -> handleTrackEnd()
                        else -> {}
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) startPositionUpdates() else stopPositionUpdates()
                }
            })
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                _playerState.update {
                    it.copy(currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L))
                }
                delay(250L)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun handleTrackEnd() {
        val state = _playerState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
            RepeatMode.ALL -> next()
            RepeatMode.OFF -> {
                if (state.currentIndex < state.queue.lastIndex) {
                    next()
                } else {
                    _playerState.update { it.copy(isPlaying = false, currentPosition = 0L) }
                }
            }
        }
    }

    fun play(song: SongInfo) {
        val url = song.streamUrl ?: return
        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()

        val currentQueue = _playerState.value.queue.toMutableList()
        val existingIndex = currentQueue.indexOfFirst { it.id == song.id }
        if (existingIndex >= 0) {
            _playerState.update {
                it.copy(currentSong = song, currentIndex = existingIndex)
            }
        } else {
            currentQueue.add(song)
            _playerState.update {
                it.copy(
                    currentSong = song,
                    queue = currentQueue,
                    currentIndex = currentQueue.lastIndex
                )
            }
        }
    }

    fun playQueue(songs: List<SongInfo>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        _playerState.update {
            it.copy(queue = songs, currentIndex = startIndex)
        }
        val song = songs[startIndex]
        val url = song.streamUrl ?: return
        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()
        _playerState.update { it.copy(currentSong = song) }
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun resume() {
        exoPlayer.play()
    }

    fun next() {
        val state = _playerState.value
        if (state.queue.isEmpty()) return

        val nextIndex = if (state.shuffleEnabled) {
            if (state.queue.size <= 1) 0
            else {
                var rand: Int
                do { rand = (state.queue.indices).random() } while (rand == state.currentIndex)
                rand
            }
        } else {
            val candidate = state.currentIndex + 1
            if (candidate > state.queue.lastIndex) {
                if (state.repeatMode == RepeatMode.ALL) 0 else return
            } else candidate
        }

        val song = state.queue[nextIndex]
        val url = song.streamUrl ?: return
        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()
        _playerState.update {
            it.copy(currentSong = song, currentIndex = nextIndex, currentPosition = 0L)
        }
    }

    fun previous() {
        if (exoPlayer.currentPosition > 3000) {
            exoPlayer.seekTo(0)
            _playerState.update { it.copy(currentPosition = 0L) }
            return
        }

        val state = _playerState.value
        if (state.queue.isEmpty()) return

        val prevIndex = if (state.currentIndex > 0) {
            state.currentIndex - 1
        } else {
            if (state.repeatMode == RepeatMode.ALL) state.queue.lastIndex else 0
        }

        val song = state.queue[prevIndex]
        val url = song.streamUrl ?: return
        exoPlayer.stop()
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()
        _playerState.update {
            it.copy(currentSong = song, currentIndex = prevIndex, currentPosition = 0L)
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        _playerState.update { it.copy(currentPosition = position) }
    }

    fun toggleShuffle() {
        _playerState.update { it.copy(shuffleEnabled = !it.shuffleEnabled) }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _playerState.update { it.copy(repeatMode = mode) }
    }

    fun cycleRepeatMode() {
        val next = when (_playerState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatMode(next)
    }

    fun addToQueue(song: SongInfo) {
        _playerState.update {
            it.copy(queue = it.queue + song)
        }
    }

    fun clearQueue() {
        exoPlayer.stop()
        stopPositionUpdates()
        _playerState.value = PlayerState()
    }

    fun release() {
        stopPositionUpdates()
        exoPlayer.release()
    }
}
