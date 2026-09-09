package dev.ahmedmohamed.hayaitts.ui.player

import dev.ahmedmohamed.hayaitts.core.result.Outcome
import dev.ahmedmohamed.hayaitts.domain.model.Story
import dev.ahmedmohamed.hayaitts.domain.repo.Narrator
import dev.ahmedmohamed.hayaitts.domain.repo.StoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * "Start Listening should just play."
 *
 * Asserted as behaviour rather than by reading the source: the player must
 * ask the narrator to start without anything calling `togglePlayPause`
 * first. The screen it replaced opened paused behind a "Preparing
 * narration…" label, which fails both assertions here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerAutoStartTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeNarrator : Narrator {
        var startCount = 0
        var lastVoiceId: String? = null
        var lastSid: Int? = null
        var lastText: String? = null
        override fun start(
            scope: CoroutineScope,
            voiceId: String,
            sid: Int,
            text: String,
            speed: Float,
            startUnit: Int,
            listener: Narrator.Listener,
        ) {
            startCount++
            lastVoiceId = voiceId
            lastSid = sid
            lastText = text
        }
        override suspend fun stop() = Unit
        override fun cancel() = Unit
    }

    private class FakeStories(private val text: String) : StoryRepository {
        override val stories: Flow<List<Story>> = flowOf(
            listOf(Story(id = 1, title = "A story", sizeBytes = 10, importedAtMillis = 0, path = "/tmp/a")),
        )
        override suspend fun import(rawUri: String): Outcome<Story> =
            Outcome.Success(Story(1, "A story", 10, 0, "/tmp/a"))
        override suspend fun readText(id: Long): Outcome<String> = Outcome.Success(text)
        override suspend fun rename(id: Long, title: String): Outcome<Unit> = Outcome.Success(Unit)
        override suspend fun delete(id: Long): Outcome<Unit> = Outcome.Success(Unit)
    }

    private fun viewModel(narrator: Narrator, text: String = "A sentence. And another one.") =
        PlayerViewModel(
            storyId = 1L,
            voiceId = "kokoro-en-v0_19",
            sid = 5,
            stories = FakeStories(text),
            narrator = narrator,
        )

    @Test
    fun `playback starts without anyone pressing play`() = runTest(dispatcher) {
        val narrator = FakeNarrator()
        val vm = viewModel(narrator)
        advanceUntilIdle()

        assertEquals("the player did not start on its own", 1, narrator.startCount)
        assertTrue(vm.uiState.value.isPlaying)
    }

    @Test
    fun `the chosen speaker reaches synthesis`() = runTest(dispatcher) {
        // The regression this replaces: the player hardcoded sid = 0, so the
        // speaker picked in the previous step was silently discarded.
        val narrator = FakeNarrator()
        viewModel(narrator)
        advanceUntilIdle()

        assertEquals("kokoro-en-v0_19", narrator.lastVoiceId)
        assertEquals(5, narrator.lastSid)
    }

    @Test
    fun `an unreadable story does not start playback`() = runTest(dispatcher) {
        val narrator = FakeNarrator()
        val failing = object : StoryRepository by FakeStories("") {
            override suspend fun readText(id: Long): Outcome<String> =
                Outcome.Failure(dev.ahmedmohamed.hayaitts.core.result.AppError.Storage)
        }
        val vm = PlayerViewModel(1L, "v", 0, failing, narrator)
        advanceUntilIdle()

        assertEquals(0, narrator.startCount)
        assertTrue(vm.uiState.value.failed)
    }
}
