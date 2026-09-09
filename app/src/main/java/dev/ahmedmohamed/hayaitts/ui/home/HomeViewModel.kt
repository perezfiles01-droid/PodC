package dev.ahmedmohamed.hayaitts.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ahmedmohamed.hayaitts.core.result.AppError
import dev.ahmedmohamed.hayaitts.core.result.Outcome
import dev.ahmedmohamed.hayaitts.domain.model.Story
import dev.ahmedmohamed.hayaitts.domain.repo.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Home tab's story list and the upload card.
 *
 * The repository returns [Outcome], so every rejection arrives as a value
 * rather than an exception; this maps them onto the enum the screen turns
 * into a localized message.
 */
class HomeViewModel(
    private val stories: StoryRepository,
) : ViewModel() {

    /** Why the last import did not go through. Cleared once shown. */
    enum class ImportError { TooLarge, WrongType, Empty, Unreadable }

    val recent: StateFlow<List<Story>> = stories.stories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _importError = MutableStateFlow<ImportError?>(null)
    val importError: StateFlow<ImportError?> = _importError.asStateFlow()

    fun import(rawUri: String) {
        viewModelScope.launch {
            when (val outcome = stories.import(rawUri)) {
                is Outcome.Success -> _importError.value = null
                is Outcome.Failure -> _importError.value = outcome.error.toImportError()
            }
        }
    }

    fun consumeImportError() {
        _importError.value = null
    }

    fun rename(id: Long, title: String) {
        viewModelScope.launch { stories.rename(id, title) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { stories.delete(id) }
    }

    private fun AppError.toImportError(): ImportError = when (this) {
        is AppError.Validation -> when (reason) {
            "too_large" -> ImportError.TooLarge
            "wrong_type" -> ImportError.WrongType
            "empty" -> ImportError.Empty
            else -> ImportError.Unreadable
        }
        else -> ImportError.Unreadable
    }
}
