package dev.ahmedmohamed.hayaitts.domain.model

/**
 * Splits narration text into small synthesis units.
 *
 * The unit size is what decides how long the listener waits for the first
 * sound: the engine cannot emit audio for a unit until it has generated all
 * of it, so a chapter-sized unit means a chapter-sized wait. Units of a few
 * sentences keep time-to-first-audio near the cost of one sentence while
 * still giving the model enough context to get prosody right.
 *
 * Pure Kotlin so the boundaries can be asserted without a device.
 */
object SentenceSplitter {

    /** Units are grown to about this many characters, then closed. */
    const val TARGET_CHARS: Int = 250

    /**
     * The first unit is deliberately smaller: it is the only one the listener
     * actually waits for, since every later unit renders while its
     * predecessor plays.
     */
    const val FIRST_UNIT_CHARS: Int = 90

    /**
     * Abbreviations whose trailing period does not end a sentence. Without
     * this, "Mr. Vance" and "3.5 hours" both split mid-sentence, and the
     * seam is audible as a swallowed word.
     */
    private val ABBREVIATIONS = setOf(
        "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "st", "mt", "rev",
        "vs", "etc", "eg", "ie", "no", "vol", "fig", "al", "inc", "ltd",
        "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "sept",
        "oct", "nov", "dec",
    )

    /**
     * Sentence boundaries in [text], as end offsets. A boundary is a
     * terminator followed by whitespace, where the terminator is not part of
     * an abbreviation or a decimal number.
     */
    fun sentences(text: String): List<String> {
        val out = mutableListOf<String>()
        var start = 0
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '.' || c == '!' || c == '?' || c == '\n') {
                // Consume any run of terminators and closing quotes/brackets
                // so `?!"` stays with the sentence it ends.
                var end = i + 1
                while (end < text.length && text[end] in TRAILING) end++
                val atEnd = end >= text.length
                val followedByBreak = atEnd || text[end].isWhitespace()
                if (followedByBreak && !isFalseStop(text, i) && startsNewSentence(text, end)) {
                    val piece = text.substring(start, end).trim()
                    if (piece.isNotEmpty()) out += piece
                    start = end
                    i = end
                    continue
                }
                i = end
                continue
            }
            i++
        }
        val tail = text.substring(start).trim()
        if (tail.isNotEmpty()) out += tail
        return out
    }

    /** Groups [text]'s sentences into units, smallest first. */
    fun units(
        text: String,
        targetChars: Int = TARGET_CHARS,
        firstUnitChars: Int = FIRST_UNIT_CHARS,
    ): List<String> {
        val sentences = sentences(text)
        if (sentences.isEmpty()) return emptyList()

        val out = mutableListOf<String>()
        var buffer = StringBuilder()
        for (sentence in sentences) {
            val limit = if (out.isEmpty()) firstUnitChars else targetChars
            if (buffer.isEmpty()) {
                buffer.append(sentence)
            } else if (buffer.length + 1 + sentence.length <= limit) {
                buffer.append(' ').append(sentence)
            } else {
                out += buffer.toString()
                buffer = StringBuilder(sentence)
            }
            if (buffer.length >= limit) {
                out += buffer.toString()
                buffer = StringBuilder()
            }
        }
        if (buffer.isNotEmpty()) out += buffer.toString()
        return out
    }

    private val TRAILING = setOf('.', '!', '?', '"', '\'', ')', ']', '”', '’', '»')

    /**
     * True when the text after [from] reads like the start of a new
     * sentence. A terminator followed by a lower-case word is a
     * continuation, not a boundary — `"Who's there?" she asked.` is one
     * sentence, and splitting at the quote cuts it in half mid-thought.
     */
    private fun startsNewSentence(text: String, from: Int): Boolean {
        var j = from
        while (j < text.length && text[j].isWhitespace()) j++
        if (j >= text.length) return true
        val c = text[j]
        return !c.isLowerCase()
    }

    /** True when the terminator at [index] does not end a sentence. */
    private fun isFalseStop(text: String, index: Int): Boolean {
        if (text[index] != '.') return false
        // A decimal point: digit before and after.
        val prev = text.getOrNull(index - 1)
        val next = text.getOrNull(index + 1)
        if (prev != null && prev.isDigit() && next != null && next.isDigit()) return true
        // An abbreviation: the word immediately before the period.
        var start = index
        while (start > 0 && text[start - 1].isLetter()) start--
        val word = text.substring(start, index).lowercase()
        return word.isNotEmpty() && word in ABBREVIATIONS
    }
}
