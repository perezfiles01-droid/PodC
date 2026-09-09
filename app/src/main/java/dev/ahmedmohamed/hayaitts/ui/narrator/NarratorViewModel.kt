package dev.ahmedmohamed.hayaitts.ui.narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ahmedmohamed.hayaitts.domain.model.InstalledVoice
import dev.ahmedmohamed.hayaitts.domain.model.NarratorFilter
import dev.ahmedmohamed.hayaitts.domain.model.filteredBy
import dev.ahmedmohamed.hayaitts.domain.repo.StoryRepository
import dev.ahmedmohamed.hayaitts.domain.repo.VoiceRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Step 2 of the listening flow: pick which installed voice narrates.
 *
 * Only installed voices are offered — synthesis needs the model on disk, so
 * offering a catalog entry here would dead-end at "download first".
 */
class NarratorViewModel(
    private val storyId: Long,
    private val voices: VoiceRepository,
    stories: StoryRepository,
) : ViewModel() {

    data class UiState(
        val storyTitle: String = "",
        val voices: List<InstalledVoice> = emptyList(),
        val filter: NarratorFilter = NarratorFilter.ALL,
        val selectedVoiceId: String? = null,
    ) {
        val canStart: Boolean get() = selectedVoiceId != null
    }

    private val filter = MutableStateFlow(NarratorFilter.ALL)
    private val selected = MutableStateFlow<String?>(null)

    private val title = stories.stories
        .map { list -> list.firstOrNull { it.id == storyId }?.title.orEmpty() }

    val uiState: StateFlow<UiState> = combine(
        voices.installed,
        filter,
        selected,
        title,
    ) { installed, chip, selectedId, storyTitle ->
        val visible = installed.filteredBy(chip)
        UiState(
            storyTitle = storyTitle,
            voices = visible,
            filter = chip,
            // Keep the selection only while it is still on screen, so the
            // Start button can never fire on a voice the filter has hidden.
            selectedVoiceId = selectedId?.takeIf { id -> visible.any { it.voiceId == id } }
                ?: visible.firstOrNull()?.voiceId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun setFilter(chip: NarratorFilter) = filter.update { chip }

    fun select(voiceId: String) = selected.update { voiceId }
}
