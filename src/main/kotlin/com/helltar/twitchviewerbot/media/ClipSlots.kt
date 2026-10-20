package com.helltar.twitchviewerbot.media

import com.helltar.twitchviewerbot.Config
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Global cap on simultaneous recordings across all users.
 *
 * Every clip runs its own streamlink + ffmpeg pair (~60-110 MB RAM together), so the per-request
 * batching alone doesn't bound memory: several users clipping at once would stack processes until
 * the container is OOM-killed. A clip waits here for a free slot; waiting costs only a suspended
 * coroutine, and /cancel still works because Semaphore.acquire is cancellable.
 */
object ClipSlots {

    val limit: Int = Config.maxConcurrentClips

    private val semaphore = Semaphore(limit)

    val available: Int
        get() = semaphore.availablePermits

    suspend fun <T> withSlot(block: suspend () -> T): T =
        semaphore.withPermit { block() }
}
