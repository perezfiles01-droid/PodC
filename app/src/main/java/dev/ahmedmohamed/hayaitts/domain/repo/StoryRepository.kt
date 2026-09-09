package dev.ahmedmohamed.hayaitts.domain.repo

import dev.ahmedmohamed.hayaitts.core.result.Outcome
import dev.ahmedmohamed.hayaitts.domain.model.Story
import kotlinx.coroutines.flow.Flow

/**
 * Imported text files. Like every repository here, failures come back as
 * [Outcome.Failure] rather than thrown exceptions.
 */
interface StoryRepository {
    /** Most recently imported first. */
    val stories: Flow<List<Story>>

    /**
     * Copies the document at [rawUri] into app storage and records it.
     * Rejects anything over the size cap or outside the allowed extensions.
     */
    suspend fun import(rawUri: String): Outcome<Story>

    /** Full text of a previously imported story. */
    suspend fun readText(id: Long): Outcome<String>

    suspend fun rename(id: Long, title: String): Outcome<Unit>

    /** Removes the row and the copied file. */
    suspend fun delete(id: Long): Outcome<Unit>
}
