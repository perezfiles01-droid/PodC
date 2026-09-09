package dev.ahmedmohamed.hayaitts.domain.repo

import kotlinx.coroutines.CoroutineScope

/**
 * What the player needs from a narration pipeline.
 *
 * The seam exists so "playback starts without a tap" is assertable: it is a
 * behaviour, and behaviours asserted only by reading the source rot the
 * moment someone reshapes the code without changing its text.
 */
interface Narrator {

    /** Listener the pipeline reports progress to. */
    interface Listener {
        fun onFirstAudio(millisSinceStart: Long) {}
        fun onUnitStarted(index: Int, total: Int) {}
        fun onFinished() {}
        fun onFailed(cause: Throwable) {}
    }

    fun start(
        scope: CoroutineScope,
        voiceId: String,
        sid: Int,
        text: String,
        speed: Float,
        startUnit: Int = 0,
        listener: Listener = object : Listener {},
    )

    /** Suspending stop, for user-initiated pauses. */
    suspend fun stop()

    /** Synchronous stop, for teardown when the scope is already cancelled. */
    fun cancel()
}
