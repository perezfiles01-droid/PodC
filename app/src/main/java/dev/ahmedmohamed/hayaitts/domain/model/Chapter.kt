package dev.ahmedmohamed.hayaitts.domain.model

/** One narratable span of an imported story. */
data class Chapter(
    val index: Int,
    val title: String,
    val text: String,
)

/**
 * Splits a story into chapters for the listening screen.
 *
 * Synthesis is chunked so playback can start before the whole file is
 * rendered; the chunk boundary doubles as the chapter boundary the player's
 * chapter list shows. Kept pure so the split can be asserted without a
 * device or an engine.
 */
object ChapterSplitter {

    /**
     * Paragraphs are merged up to roughly this many characters so a chapter
     * is a useful listening unit rather than one sentence. A single paragraph
     * longer than this becomes its own chapter rather than being cut
     * mid-sentence.
     */
    const val TARGET_CHARS: Int = 1_800

    private val HEADING = Regex("""^\s{0,3}(#{1,6}\s+\S.*|(?:CHAPTER|Chapter|PART|Part)\s+[\dIVXLC]+.*)$""")

    fun split(text: String, targetChars: Int = TARGET_CHARS): List<Chapter> {
        val paragraphs = text
            .replace("\r\n", "\n")
            .split(Regex("\n\\s*\n"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (paragraphs.isEmpty()) return emptyList()

        val chunks = mutableListOf<MutableList<String>>()
        for (paragraph in paragraphs) {
            val isHeading = HEADING.matches(paragraph)
            val current = chunks.lastOrNull()
            val wouldOverflow = current != null &&
                current.sumOf { it.length + 2 } + paragraph.length > targetChars
            if (current == null || isHeading || wouldOverflow) {
                chunks += mutableListOf(paragraph)
            } else {
                current += paragraph
            }
        }

        return chunks.mapIndexed { index, parts ->
            Chapter(
                index = index,
                title = titleFor(parts.first(), index),
                text = parts.joinToString("\n\n"),
            )
        }
    }

    /** First line, trimmed of heading marks, capped so it fits a list row. */
    private fun titleFor(firstParagraph: String, index: Int): String {
        val line = firstParagraph.lineSequence().first().trim().trimStart('#', ' ')
        val short = if (line.length <= 60) line else line.take(57).trimEnd() + "…"
        return short.ifBlank { "${index + 1}" }
    }
}
