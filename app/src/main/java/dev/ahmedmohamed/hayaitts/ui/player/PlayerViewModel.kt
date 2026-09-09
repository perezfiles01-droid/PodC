package dev.ahmedmohamed.hayaitts.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ahmedmohamed.hayaitts.core.result.Outcome
import dev.ahmedmohamed.hayaitts.domain.repo.Narrator
import dev.ahmedmohamed.hayaitts.data.playground.VoiceTuning
import dev.ahmedmohamed.hayaitts.domain.model.Chapter
import dev.ahmedmohamed.hayaitts.domain.model.ChapterSplitter
import dev.ahmedmohamed.hayaitts.domain.model.SentenceSplitter
import dev.ahmedmohamed.hayaitts.domain.repo.StoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Step 3: narrate an imported story.
 *
 * Narration is streamed sentence-unit by sentence-unit through the
 * [Narrator], so audio starts about one short sentence after the
 * screen opens rather than one chapter, and a 50,000-word story costs the
 * same to start as a 500-word one. There is deliberately no "preparing"
 * state: playback begins on its own when the screen opens, and the transport
 * is there to stop it, not to start it.
 *
 * Chapters remain the navigation unit. Because narration is continuous
 * across chapter boundaries, seeking maps a chapter onto the sentence unit
 * it begins at and restarts the stream there.
 */
class PlayerViewModel(
    private val storyId: Long,
    private val voiceId: String,
    private val sid: Int,
    private val stories: StoryRepository,
    private val narrator: Narrator,
) : ViewModel() {

    data class UiState(
        val title: String = "",
        val chapters: List<Chapter> = emptyList(),
        val currentIndex: Int = 0,
        val isPlaying: Boolean = false,
        val speed: Float = 1.0f,
        /** Remaining sleep-timer minutes, or null when off. */
        val sleepMinutes: Int? = null,
        val failed: Boolean = false,
        /** Milliseconds from tap to first audio, once known. */
        val firstAudioMillis: Long? = null,
    ) {
        val current: Chapter? get() = chapters.getOrNull(currentIndex)
        val hasNext: Boolean get() = currentIndex < chapters.lastIndex
        val hasPrevious: Boolean get() = currentIndex > 0
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Full story text, held once: at the 5 MB import cap this is bounded. */
    private var text: String = ""

    /** Sentence-unit index each chapter starts at, for seeking. */
    private var chapterUnitStarts: List<Int> = emptyList()

    private var sleepTimer: Job? = null

    private val listener = object : Narrator.Listener {
        override fun onFirstAudio(millisSinceStart: Long) {
            _uiState.update { it.copy(firstAudioMillis = millisSinceStart) }
        }

        override fun onUnitStarted(index: Int, total: Int) {
            // Map the unit back onto a chapter so the readout follows the
            // narration without the two being separately tracked.
            val chapterIndex = chapterUnitStarts
                .indexOfLast { start -> start <= index }
                .coerceAtLeast(0)
            _uiState.update { it.copy(currentIndex = chapterIndex) }
        }

        override fun onFinished() {
            _uiState.update { it.copy(isPlaying = false) }
        }

        override fun onFailed(cause: Throwable) {
            _uiState.update { it.copy(isPlaying = false, failed = true) }
        }
    }

    init {
        viewModelScope.launch {
            when (val loaded = stories.readText(storyId)) {
                is Outcome.Success -> {
                    text = loaded.value
                    val chapters = ChapterSplitter.split(text)
                    chapterUnitStarts = unitStartsFor(chapters)
                    _uiState.update { it.copy(chapters = chapters) }
                    // Start Listening plays. Nothing else has to be tapped.
                    if (chapters.isNotEmpty()) play()
                }
                is Outcome.Failure -> _uiState.update { it.copy(failed = true) }
            }
        }
        viewModelScope.launch {
            stories.stories.collect { list ->
                val match = list.firstOrNull { story -> story.id == storyId }
                if (match != null) _uiState.update { it.copy(title = match.title) }
            }
        }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) pause() else play()
    }

    fun play() {
        if (text.isBlank()) return
        _uiState.update { it.copy(isPlaying = true, failed = false) }
        narrator.start(
            scope = viewModelScope,
            voiceId = voiceId,
            sid = sid,
            text = text,
            speed = _uiState.value.speed,
            startUnit = chapterUnitStarts.getOrElse(_uiState.value.currentIndex) { 0 },
            listener = listener,
        )
    }

    fun pause() {
        viewModelScope.launch { narrator.stop() }
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun skipToNext() = seekTo(_uiState.value.currentIndex + 1)

    fun skipToPrevious() = seekTo(_uiState.value.currentIndex - 1)

    fun seekTo(index: Int) {
        if (index !in _uiState.value.chapters.indices) return
        val wasPlaying = _uiState.value.isPlaying
        viewModelScope.launch {
            narrator.stop()
            _uiState.update { it.copy(currentIndex = index, isPlaying = false) }
            if (wasPlaying) play()
        }
    }

    fun setSpeed(speed: Float) {
        val clamped = speed.coerceIn(VoiceTuning.SPEED_MIN, VoiceTuning.SPEED_MAX)
        val wasPlaying = _uiState.value.isPlaying
        viewModelScope.launch {
            narrator.stop()
            // Speed is a synthesis parameter, so the stream restarts from the
            // current chapter rather than being resampled.
            _uiState.update { it.copy(speed = clamped, isPlaying = false) }
            if (wasPlaying) play()
        }
    }

    /** Pass null to cancel. */
    fun setSleepTimer(minutes: Int?) {
        sleepTimer?.cancel()
        _uiState.update { it.copy(sleepMinutes = minutes) }
        if (minutes == null) return
        sleepTimer = viewModelScope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60_000L)
                remaining--
                _uiState.update { it.copy(sleepMinutes = remaining) }
            }
            pause()
            _uiState.update { it.copy(sleepMinutes = null) }
        }
    }

    /**
     * The sentence-unit index each chapter begins at. Chapters and units are
     * split by different rules, so the mapping is computed by counting the
     * units each chapter's own text produces rather than assumed.
     */
    private fun unitStartsFor(chapters: List<Chapter>): List<Int> {
        var running = 0
        return chapters.map { chapter ->
            val start = running
            running += SentenceSplitter.units(chapter.text).size
            start
        }
    }

    override fun onCleared() {
        sleepTimer?.cancel()
        // Synchronous: viewModelScope is already cancelled here, so a
        // launched stop would never run and the track would play on.
        narrator.cancel()
        super.onCleared()
    }
}
