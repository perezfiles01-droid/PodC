package dev.ahmedmohamed.hayaitts.data.narration

import dev.ahmedmohamed.hayaitts.core.dispatchers.DispatcherProvider
import dev.ahmedmohamed.hayaitts.domain.model.SentenceSplitter
import dev.ahmedmohamed.hayaitts.domain.repo.NarrationEngine
import dev.ahmedmohamed.hayaitts.domain.repo.Narrator
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The property under test is a timing one, and it is the whole point of the
 * change: **audio must reach the speaker before the last unit has been
 * synthesized.** The previous implementation rendered a whole chapter and
 * only then played it, which fails this by construction — that is what the
 * user saw as "Preparing narration…".
 *
 * Driven by a fake engine and a recording sink, because the real ones need a
 * device and a pair of ears.
 */
class StreamingNarratorTest {

    private object TestDispatchers : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.IO
        override val default: CoroutineDispatcher = Dispatchers.Default
        override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
    }

    /** Emits [chunksPerUnit] chunks per unit, counting units synthesized. */
    private class FakeEngine(
        private val chunksPerUnit: Int = 2,
        private val onUnit: (Int) -> Unit = {},
    ) : NarrationEngine {
        val unitsSynthesized = AtomicInteger(0)
        override suspend fun sampleRateOf(voiceId: String): Int = 22_050
        override suspend fun stream(
            voiceId: String,
            sid: Int,
            text: String,
            speed: Float,
            onChunk: (FloatArray) -> Boolean,
        ): Int {
            val n = unitsSynthesized.incrementAndGet()
            onUnit(n)
            repeat(chunksPerUnit) {
                if (!onChunk(FloatArray(64) { 0.1f })) return 22_050
            }
            return 22_050
        }
    }

    private class RecordingSink(
        private val onWrite: (Int) -> Unit = {},
    ) : AudioSink {
        val writes = AtomicInteger(0)
        var maxObservedBacklog = 0
        override fun start(sampleRate: Int) = Unit
        override fun write(samples: FloatArray) {
            onWrite(writes.incrementAndGet())
        }
        override fun stop() = Unit
    }

    private val story = ("The quick brown fox jumped over the lazy dog. ").repeat(200)

    @Test
    fun `audio starts before the last unit is synthesized`() {
        val totalUnits = SentenceSplitter.units(story).size
        assertTrue("fixture should span many units", totalUnits > 10)

        val firstWrite = CountDownLatch(1)
        val unitsAtFirstWrite = AtomicInteger(-1)
        lateinit var engine: FakeEngine

        val sink = RecordingSink { count ->
            if (count == 1) {
                unitsAtFirstWrite.set(engine.unitsSynthesized.get())
                firstWrite.countDown()
            }
        }
        engine = FakeEngine()
        val narrator = StreamingNarrator(engine, sink, TestDispatchers)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        narrator.start(
            scope, voiceId = "v", sid = 0, text = story, speed = 1f,
            startUnit = 0, listener = object : Narrator.Listener {},
        )
        assertTrue(
            "no audio was written within 5s",
            firstWrite.await(5, TimeUnit.SECONDS),
        )

        // The assertion the blocking implementation cannot satisfy: the very
        // first write happened while most of the story was still unrendered.
        val rendered = unitsAtFirstWrite.get()
        assertTrue("first audio observed after $rendered units", rendered in 1 until totalUnits)
        runBlocking { narrator.stop() }
    }

    @Test
    fun `synthesis stays a bounded distance ahead of playback`() {
        // A 50,000-word narration must not render ahead without limit, or
        // memory grows with the length of the book. The producer is only
        // allowed to lead by the queue bound plus the unit in flight.
        val long = ("The quick brown fox jumped over the lazy dog. ").repeat(3_000)
        val writes = AtomicInteger(0)
        val maxLead = AtomicInteger(0)
        lateinit var engine: FakeEngine

        val sink = RecordingSink { count ->
            writes.set(count)
            // A slow consumer: without back-pressure the producer would run
            // to the end of the book while this sleeps.
            Thread.sleep(1)
        }
        engine = FakeEngine(chunksPerUnit = 1, onUnit = { unitNo ->
            val lead = unitNo - writes.get()
            maxLead.updateAndGet { previous -> maxOf(previous, lead) }
        })

        val narrator = StreamingNarrator(engine, sink, TestDispatchers)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        narrator.start(
            scope, voiceId = "v", sid = 0, text = long, speed = 1f,
            startUnit = 0, listener = object : Narrator.Listener {},
        )
        Thread.sleep(1_500)
        runBlocking { narrator.stop() }

        assertTrue("nothing played", writes.get() > 0)
        assertTrue(
            "producer ran ${maxLead.get()} units ahead; the queue bound should hold it near 4",
            maxLead.get() <= 16,
        )
    }

    @Test(timeout = 20_000)
    fun `stop returns promptly when the queue is full`() {
        // The hang this guards against: with the queue full and the consumer
        // parked in a write, a blocking put leaves the engine's callback
        // thread stuck forever, so cancelAndJoin never returns and pause
        // hangs. It cost a 40-minute CI run before it was found.
        //
        // The sink models AudioTrack: write blocks until stop() is called.
        val released = CountDownLatch(1)
        val blockingSink = object : AudioSink {
            override fun start(sampleRate: Int) = Unit
            override fun write(samples: FloatArray) {
                released.await(10, TimeUnit.SECONDS)
            }
            override fun stop() {
                released.countDown()
            }
        }
        val narrator = StreamingNarrator(FakeEngine(chunksPerUnit = 8), blockingSink, TestDispatchers)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        narrator.start(
            scope, voiceId = "v", sid = 0, text = story, speed = 1f,
            startUnit = 0, listener = object : Narrator.Listener {},
        )
        Thread.sleep(500) // let the queue fill and the consumer block

        val startedAt = System.currentTimeMillis()
        runBlocking { narrator.stop() }
        val elapsed = System.currentTimeMillis() - startedAt
        assertTrue("stop() took ${elapsed}ms; it must not block on a full queue", elapsed < 5_000)
    }

    @Test
    fun `an empty story finishes without opening the sink`() {
        val sink = RecordingSink()
        val narrator = StreamingNarrator(FakeEngine(), sink, TestDispatchers)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val finished = CountDownLatch(1)
        narrator.start(
            scope, voiceId = "v", sid = 0, text = "   ", speed = 1f,
            listener = object : Narrator.Listener {
                override fun onFinished() = finished.countDown()
            },
        )
        assertTrue(finished.await(2, TimeUnit.SECONDS))
        assertEquals(0, sink.writes.get())
    }
}
