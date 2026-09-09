package dev.ahmedmohamed.hayaitts.ui.narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ahmedmohamed.hayaitts.domain.model.NarratorFilter
import dev.ahmedmohamed.hayaitts.domain.model.NarratorOption
import dev.ahmedmohamed.hayaitts.domain.model.filteredBy
import dev.ahmedmohamed.hayaitts.domain.model.toNarratorOptions
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
        /** One row per speaker, not per voice. */
        val options: List<NarratorOption> = emptyList(),
        val filter: NarratorFilter = NarratorFilter.ALL,
        val selected: NarratorOption? = null,
    ) {
        val canStart: Boolean get() = selected != null
    }

    private val filter = MutableStateFlow(NarratorFilter.ALL)
    /** Selection key: a voice alone is not enough now that sid matters. */
    private val selected = MutableStateFlow<Pair<String, Int>?>(null)

    private val title = stories.stories
        .map { list -> list.firstOrNull { it.id == storyId }?.title.orEmpty() }

    val uiState: StateFlow<UiState> = combine(
        voices.installed,
        filter,
        selected,
        title,
    ) { installed, chip, selectedKey, storyTitle ->
        val visible = installed.toNarratorOptions().filteredBy(chip)
        UiState(
            storyTitle = storyTitle,
            options = visible,
            filter = chip,
            // Keep the selection only while it is still on screen, so Start
            // can never fire on a speaker the filter has hidden.
            selected = visible.firstOrNull { (it.voiceId to it.sid) == selectedKey }
                ?: visible.firstOrNull(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun setFilter(chip: NarratorFilter) = filter.update { chip }

    fun select(option: NarratorOption) = selected.update { option.voiceId to option.sid }
}
