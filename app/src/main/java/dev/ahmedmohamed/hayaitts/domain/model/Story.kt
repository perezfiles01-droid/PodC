package dev.ahmedmohamed.hayaitts.domain.model

/**
 * One imported text file, ready to be narrated.
 *
 * The text itself lives on disk under `filesDir/stories/<id>.txt`; only the
 * metadata Home renders is held here.
 */
data class Story(
    val id: Long = 0L,
    /** Display name, taken from the picked document. */
    val title: String,
    val sizeBytes: Long,
    val importedAtMillis: Long,
    /** Absolute path of the copied text file. */
    val path: String,
)

/** Import rules, kept pure so they can be asserted without a device. */
object StoryLimits {
    /** The mockup states .txt only, 5 MB max; both are enforced on import. */
    const val MAX_BYTES: Long = 5L * 1024 * 1024

    val ALLOWED_EXTENSIONS: Set<String> = setOf("txt", "text", "md")

    /**
     * Returns null when [name] / [sizeBytes] may be imported, or the reason
     * it may not. The reason is an enum rather than a message so the UI picks
     * the localized string.
     *
     * A **negative** [sizeBytes] means "not reported": document providers are
     * not obliged to fill in `OpenableColumns.SIZE`, and treating that as an
     * empty file would reject perfectly good documents. Size is re-checked
     * against the copied file, which is the only authoritative length.
     */
    fun rejectionFor(name: String, sizeBytes: Long): Rejection? = when {
        extensionOf(name) !in ALLOWED_EXTENSIONS -> Rejection.WrongType
        sizeBytes == 0L -> Rejection.Empty
        sizeBytes > MAX_BYTES -> Rejection.TooLarge
        else -> null
    }

    /** Post-copy check against the real file length. */
    fun rejectionForCopied(sizeBytes: Long): Rejection? = when {
        sizeBytes <= 0L -> Rejection.Empty
        sizeBytes > MAX_BYTES -> Rejection.TooLarge
        else -> null
    }

    fun extensionOf(name: String): String =
        name.substringAfterLast('.', "").lowercase().trim()

    enum class Rejection { Empty, TooLarge, WrongType }
}
