package dev.ahmedmohamed.hayaitts.data.narration

import co.touchlab.kermit.Logger
import dev.ahmedmohamed.hayaitts.core.dispatchers.DispatcherProvider
import dev.ahmedmohamed.hayaitts.domain.model.SentenceSplitter
import dev.ahmedmohamed.hayaitts.domain.repo.NarrationEngine
import dev.ahmedmohamed.hayaitts.domain.repo.Narrator
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Turns a long text into continuous audio without ever rendering it whole.
 *
 * A producer walks [SentenceSplitter] units through the engine and hands each
 * chunk to a small bounded queue as it lands; a consumer writes them to the
 * sink in order. The queue bound is what keeps a 50,000-word story the same
 * shape as a 500-word one: when it is full the engine's callback thread
 * blocks inside `put`, so synthesis runs a fixed distance ahead of the
 * speaker and holds a fixed amount of audio no matter how long the book is.
 *
 * The queue is a plain [ArrayBlockingQueue] rather than a coroutine channel
 * on purpose. Chunks arrive on sherpa-onnx's own callback thread, which is
 * not a coroutine; bridging that with `runBlocking` risks parking a
 * dispatcher thread the consumer also needs, which deadlocks instead of
 * playing.
 */
class StreamingNarrator(
    private val engine: NarrationEngine,
    private val sink: AudioSink,
    private val dispatchers: DispatcherProvider,
) : Narrator {

    private val log = Logger.withTag("StreamingNarrator")

    private var job: Job? = null
    private val cancelled = AtomicBoolean(false)

    val isRunning: Boolean get() = job?.isActive == true

    /** Number of units [text] will be narrated in. */
    fun unitCount(text: String): Int = SentenceSplitter.units(text).size

    /**
     * Narrates [text] from unit [startUnit]. Cancels anything already
     * running. Returns immediately; progress arrives on [listener].
     */
    override fun start(
        scope: CoroutineScope,
        voiceId: String,
        sid: Int,
        text: String,
        speed: Float,
        // No defaults here: the interface declares them, and an override
        // repeating them does not compile.
        startUnit: Int,
        listener: Narrator.Listener,
    ) {
        val units = SentenceSplitter.units(text)
        if (units.isEmpty() || startUnit >= units.size) {
            listener.onFinished()
            return
        }
        job?.cancel()
        cancelled.set(false)

        job = scope.launch(dispatchers.default) {
            val startedAt = System.currentTimeMillis()
            val queue = ArrayBlockingQueue<FloatArray>(QUEUE_CAPACITY)
            val done = AtomicBoolean(false)

            // The rate must be known before the sink opens: a chunk played
            // at the wrong rate is audibly the wrong pitch and speed, and
            // the first chunk arrives too late to ask.
            val sampleRate = engine.sampleRateOf(voiceId)

            val consumer = launch(dispatchers.io) {
                var opened = false
                var reportedFirst = false
                try {
                    while (isActive && !cancelled.get()) {
                        val chunk = queue.poll(POLL_MILLIS, TimeUnit.MILLISECONDS)
                        if (chunk == null) {
                            if (done.get() && queue.isEmpty()) break
                            continue
                        }
                        if (!opened) {
                            sink.start(sampleRate)
                            opened = true
                        }
                        sink.write(chunk)
                        if (!reportedFirst) {
                            reportedFirst = true
                            val elapsed = System.currentTimeMillis() - startedAt
                            log.i { "First audio after ${elapsed}ms" }
                            listener.onFirstAudio(elapsed)
                        }
                    }
                } finally {
                    sink.stop()
                }
            }

            try {
                for (index in startUnit until units.size) {
                    if (!isActive || cancelled.get()) break
                    listener.onUnitStarted(index, units.size)
                    engine.stream(
                        voiceId = voiceId,
                        sid = sid,
                        text = units[index],
                        speed = speed,
                    ) { samples ->
                        if (cancelled.get()) {
                            false
                        } else {
                            // Blocks the engine's callback thread when the
                            // consumer is behind. This is the back-pressure,
                            // and it is the whole memory bound.
                            queue.put(samples)
                            true
                        }
                    }
                }
                done.set(true)
                consumer.join()
                if (isActive && !cancelled.get()) listener.onFinished()
            } catch (t: CancellationException) {
                done.set(true)
                throw t
            } catch (t: Throwable) {
                done.set(true)
                log.e(t) { "Narration failed" }
                listener.onFailed(t)
            }
        }
    }

    /**
     * Synchronous stop for teardown. `viewModelScope` is already cancelled by
     * the time `onCleared` runs, so a coroutine launched there never executes
     * and the AudioTrack would keep playing after the screen is gone.
     */
    override fun cancel() {
        cancelled.set(true)
        job?.cancel()
        job = null
        sink.stop()
    }

    override suspend fun stop() {
        val running = job ?: return
        job = null
        cancelled.set(true)
        withContext(dispatchers.default) {
            runCatching { running.cancelAndJoin() }
            sink.stop()
        }
    }

    private companion object {
        /**
         * Four chunks in flight. Large enough that a slow unit does not
         * underrun the track, small enough that memory is flat on a
         * multi-hour narration and that pause takes effect promptly.
         */
        const val QUEUE_CAPACITY = 4
        const val POLL_MILLIS = 50L
    }
}
