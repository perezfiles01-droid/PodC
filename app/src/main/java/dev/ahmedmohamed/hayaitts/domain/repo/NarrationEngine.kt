package dev.ahmedmohamed.hayaitts.domain.repo

/**
 * The one call the streaming narrator makes into a speech engine.
 *
 * Kept as a domain interface with no Android types so the pipeline can be
 * driven by a fake in tests: the property that matters — audio reaching the
 * speaker before the last unit is generated — is otherwise only observable
 * on a device with ears.
 */
interface NarrationEngine {
    /**
     * Output sample rate for [voiceId]. Queried once before narration
     * starts: the sink has to be opened at the right rate before the first
     * chunk is written, and a chunk played at the wrong rate is audibly the
     * wrong pitch and speed.
     */
    suspend fun sampleRateOf(voiceId: String): Int

    /**
     * Synthesizes [text] and hands each chunk to [onChunk] **as it is
     * produced**, not at the end. Returns the sample rate. Returning false
     * from [onChunk] cancels generation.
     */
    suspend fun stream(
        voiceId: String,
        sid: Int,
        text: String,
        speed: Float,
        onChunk: (FloatArray) -> Boolean,
    ): Int
}
