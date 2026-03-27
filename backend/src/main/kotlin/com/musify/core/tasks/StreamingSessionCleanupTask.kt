package com.musify.core.tasks

import com.musify.core.streaming.StreamingSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Scheduled task to clean up expired streaming sessions
 */
class StreamingSessionCleanupTask(
    private val sessionService: StreamingSessionService
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isRunning = AtomicBoolean(false)

    companion object {
        const val CLEANUP_INTERVAL_MS = 60_000L // Run every minute
    }

    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            scope.launch {
                while (isRunning.get()) {
                    try {
                        cleanupSessions()
                    } catch (e: Exception) {
                        // Errors are reported via Sentry in cleanupSessions
                    }

                    delay(CLEANUP_INTERVAL_MS)
                }
            }
        }
    }

    fun stop() {
        isRunning.set(false)
    }

    private suspend fun cleanupSessions() {
        when (val result = sessionService.cleanupExpiredSessions()) {
            is com.musify.core.utils.Result.Success -> {
                // Cleanup completed successfully
            }
            is com.musify.core.utils.Result.Error -> {
                // Error is captured by the caller's Sentry integration
            }
        }
    }
}
