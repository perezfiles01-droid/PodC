package dev.ahmedmohamed.hayaitts.data.stories

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import co.touchlab.kermit.Logger
import dev.ahmedmohamed.hayaitts.core.dispatchers.DispatcherProvider
import dev.ahmedmohamed.hayaitts.core.result.AppError
import dev.ahmedmohamed.hayaitts.core.result.Outcome
import dev.ahmedmohamed.hayaitts.data.db.dao.StoryDao
import dev.ahmedmohamed.hayaitts.data.db.entities.StoryEntity
import dev.ahmedmohamed.hayaitts.domain.model.Story
import dev.ahmedmohamed.hayaitts.domain.model.StoryLimits
import dev.ahmedmohamed.hayaitts.domain.repo.StoryRepository
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Copies picked text documents into `filesDir/stories` and records them.
 *
 * The copy is deliberate rather than holding the SAF URI: a content URI's
 * permission can be revoked and the backing file can move or be deleted, and
 * a story the user imported should still play a week later. Files are small
 * by construction — the import cap is 5 MB.
 *
 * Every failure comes back as [Outcome.Failure]; nothing throws across this
 * boundary.
 */
class StoryRepositoryImpl(
    private val context: Context,
    private val dao: StoryDao,
    private val dispatchers: DispatcherProvider,
) : StoryRepository {

    private val log = Logger.withTag("StoryRepository")

    private val dir: File
        get() = File(context.filesDir, "stories").apply { mkdirs() }

    override val stories: Flow<List<Story>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun import(rawUri: String): Outcome<Story> = withContext(dispatchers.io) {
        try {
            val uri = Uri.parse(rawUri)
            val (name, size) = queryMetadata(uri)

            when (StoryLimits.rejectionFor(name, size)) {
                StoryLimits.Rejection.Empty ->
                    return@withContext Outcome.Failure(
                        AppError.Validation(field = "story", reason = "empty"),
                    )
                StoryLimits.Rejection.TooLarge ->
                    return@withContext Outcome.Failure(
                        AppError.Validation(field = "story", reason = "too_large"),
                    )
                StoryLimits.Rejection.WrongType ->
                    return@withContext Outcome.Failure(
                        AppError.Validation(field = "story", reason = "wrong_type"),
                    )
                null -> Unit
            }

            val importedAt = System.currentTimeMillis()
            val target = File(dir, "story-$importedAt.txt")
            val copied = context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            if (copied == null) {
                return@withContext Outcome.Failure(AppError.Storage)
            }

            // The provider's SIZE column is advisory and often absent, so the
            // copied length is the authoritative one. Re-check both bounds
            // against what actually landed.
            when (StoryLimits.rejectionForCopied(target.length())) {
                StoryLimits.Rejection.Empty -> {
                    target.delete()
                    return@withContext Outcome.Failure(
                        AppError.Validation(field = "story", reason = "empty"),
                    )
                }
                StoryLimits.Rejection.TooLarge -> {
                    target.delete()
                    return@withContext Outcome.Failure(
                        AppError.Validation(field = "story", reason = "too_large"),
                    )
                }
                StoryLimits.Rejection.WrongType, null -> Unit
            }

            val entity = StoryEntity(
                title = name.substringBeforeLast('.', name),
                sizeBytes = target.length(),
                importedAt = importedAt,
                path = target.absolutePath,
            )
            val id = dao.insert(entity)
            Outcome.Success(entity.copy(id = id).toDomain())
        } catch (t: Throwable) {
            log.e(t) { "Story import failed" }
            Outcome.Failure(AppError.Storage, t)
        }
    }

    override suspend fun readText(id: Long): Outcome<String> = withContext(dispatchers.io) {
        try {
            val row = dao.byId(id) ?: return@withContext Outcome.Failure(
                AppError.Validation(field = "story", reason = "missing"),
            )
            val file = File(row.path)
            if (!file.isFile) {
                return@withContext Outcome.Failure(AppError.Storage)
            }
            Outcome.Success(file.readText())
        } catch (t: Throwable) {
            log.e(t) { "Story read failed" }
            Outcome.Failure(AppError.Storage, t)
        }
    }

    override suspend fun rename(id: Long, title: String): Outcome<Unit> =
        withContext(dispatchers.io) {
            val trimmed = title.trim()
            if (trimmed.isEmpty()) {
                return@withContext Outcome.Failure(
                    AppError.Validation(field = "title", reason = "empty"),
                )
            }
            try {
                dao.rename(id, trimmed)
                Outcome.Success(Unit)
            } catch (t: Throwable) {
                log.e(t) { "Story rename failed" }
                Outcome.Failure(AppError.Storage, t)
            }
        }

    override suspend fun delete(id: Long): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            dao.byId(id)?.let { File(it.path).delete() }
            dao.deleteById(id)
            Outcome.Success(Unit)
        } catch (t: Throwable) {
            log.e(t) { "Story delete failed" }
            Outcome.Failure(AppError.Storage, t)
        }
    }

    /** Display name + size from the document provider, with safe fallbacks. */
    private fun queryMetadata(uri: Uri): Pair<String, Long> {
        var name = uri.lastPathSegment?.substringAfterLast('/') ?: "story.txt"
        var size = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIdx >= 0 && !cursor.isNull(nameIdx)) name = cursor.getString(nameIdx)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
            }
        }
        return name to size
    }

    private fun StoryEntity.toDomain() = Story(
        id = id,
        title = title,
        sizeBytes = sizeBytes,
        importedAtMillis = importedAt,
        path = path,
    )
}
