package dev.ahmedmohamed.hayaitts.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ahmedmohamed.hayaitts.core.result.Outcome
import dev.ahmedmohamed.hayaitts.data.playground.VoiceTuning
import dev.ahmedmohamed.hayaitts.data.preview.VoicePreviewPlayer
import dev.ahmedmohamed.hayaitts.domain.model.Chapter
import dev.ahmedmohamed.hayaitts.domain.model.ChapterSplitter
import dev.ahmedmohamed.hayaitts.domain.repo.StoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Step 3: narrate an imported story, one chapter at a time.
 *
 * Synthesis is per chapter rather than per file, so playback starts after the
 * first chunk is rendered instead of after the whole book. The chapter is
 * also the seek unit — there is no sample-accurate scrub, because nothing is
 * rendered ahead of where the listener is.
 */
class PlayerViewModel(
    private val storyId: Long,
    private val voiceId: String,
    private val stories: StoryRepository,
    private val player: VoicePreviewPlayer,
) : ViewModel() {

    data class UiState(
        val title: String = "",
        val chapters: List<Chapter> = emptyList(),
        val currentIndex: Int = 0,
        val isPlaying: Boolean = false,
        /** True while a chapter is being synthesized and has no audio yet. */
        val isPreparing: Boolean = false,
        val speed: Float = 1.0f,
        /** Remaining sleep-timer minutes, or null when off. */
        val sleepMinutes: Int? = null,
        val failed: Boolean = false,
    ) {
        val current: Chapter? get() = chapters.getOrNull(currentIndex)
        val hasNext: Boolean get() = currentIndex < chapters.lastIndex
        val hasPrevious: Boolean get() = currentIndex > 0
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var playback: Job? = null
    private var sleepTimer: Job? = null

    init {
        viewModelScope.launch {
            when (val text = stories.readText(storyId)) {
                is Outcome.Success ->
                    _uiState.update { it.copy(chapters = ChapterSplitter.split(text.value)) }
                is Outcome.Failure ->
                    _uiState.update { it.copy(failed = true) }
            }
        }
        // Title tracks the same repository the Home list edits, so a rename
        // while listening is reflected here.
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
        if (_uiState.value.chapters.isEmpty()) return
        playback?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        playback = viewModelScope.launch {
            var index = _uiState.value.currentIndex
            while (index <= _uiState.value.chapters.lastIndex) {
                val chapter = _uiState.value.chapters[index]
                _uiState.update { it.copy(currentIndex = index, isPreparing = true) }
                val output = player.synthesizeTuned(
                    voiceId = voiceId,
                    text = chapter.text,
                    sid = 0,
                    tuning = VoiceTuning(speed = _uiState.value.speed),
                )
                _uiState.update { it.copy(isPreparing = false) }
                if (output == null) {
                    _uiState.update { it.copy(isPlaying = false, failed = true) }
                    return@launch
                }
                // Returns when the chapter has finished playing, so the loop
                // advances at the right moment without a completion callback.
                player.playSamples(output.samples, output.sampleRate)
                index++
            }
            _uiState.update { it.copy(isPlaying = false) }
        }
    }

    fun pause() {
        playback?.cancel()
        playback = null
        player.stop()
        _uiState.update { it.copy(isPlaying = false, isPreparing = false) }
    }

    fun skipToNext() = seekTo(_uiState.value.currentIndex + 1)

    fun skipToPrevious() = seekTo(_uiState.value.currentIndex - 1)

    fun seekTo(index: Int) {
        val chapters = _uiState.value.chapters
        if (index !in chapters.indices) return
        val wasPlaying = _uiState.value.isPlaying
        pause()
        _uiState.update { it.copy(currentIndex = index) }
        if (wasPlaying) play()
    }

    fun setSpeed(speed: Float) {
        val clamped = speed.coerceIn(VoiceTuning.SPEED_MIN, VoiceTuning.SPEED_MAX)
        val wasPlaying = _uiState.value.isPlaying
        pause()
        _uiState.update { it.copy(speed = clamped) }
        // Speed is applied at synthesis time, so the current chapter is
        // re-rendered rather than resampled.
        if (wasPlaying) play()
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

    override fun onCleared() {
        playback?.cancel()
        sleepTimer?.cancel()
        player.stop()
        super.onCleared()
    }
}
