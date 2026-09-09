package dev.ahmedmohamed.hayaitts.data.narration

import android.content.Context
import dev.ahmedmohamed.hayaitts.core.dispatchers.DispatcherProvider
import dev.ahmedmohamed.hayaitts.domain.repo.NarrationEngine
import dev.ahmedmohamed.hayaitts.tts.SherpaTtsRuntime
import kotlinx.coroutines.withContext

/**
 * Adapts [SherpaTtsRuntime.synthesizeStreaming] to [NarrationEngine].
 *
 * The runtime has had a streaming entry point since it was written and
 * nothing used it — every caller took the blocking sibling, which is why the
 * player waited for a whole chapter before making a sound.
 */
class SherpaNarrationEngine(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) : NarrationEngine {

    override suspend fun sampleRateOf(voiceId: String): Int =
        withContext(dispatchers.default) {
            SherpaTtsRuntime.get(context).sampleRateOf(voiceId)
        }

    override suspend fun stream(
        voiceId: String,
        sid: Int,
        text: String,
        speed: Float,
        onChunk: (FloatArray) -> Boolean,
    ): Int = withContext(dispatchers.default) {
        val runtime = SherpaTtsRuntime.get(context)
        runtime.synthesizeStreaming(
            voiceId = voiceId,
            text = text,
            sid = sid,
            speed = speed,
            // Pitch stays at 1.0 deliberately: the resampler is stateless
            // across chunk seams, so any shift clicks at every boundary —
            // and with unit-sized chunks there are a great many boundaries.
            pitch = 1.0f,
        ) { chunk -> if (onChunk(chunk)) 1 else 0 }
            .sampleRate
    }
}
