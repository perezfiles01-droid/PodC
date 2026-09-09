package dev.ahmedmohamed.hayaitts.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The unit boundary decides both what the listener waits for and whether the
 * narration says every word. Conservation is the invariant that matters: a
 * splitter that drops a fragment produces audio that skips it, and nothing
 * in the app would report an error.
 */
class SentenceSplitterTest {

    private fun normalise(s: String) = s.replace(Regex("\\s+"), " ").trim()

    @Test
    fun `every character survives the split`() {
        val text = """
            The library stood at the end of the street. It was always open!
            Was it? Nora had walked past it a hundred times without going in.
        """.trimIndent()
        assertEquals(normalise(text), normalise(SentenceSplitter.units(text).joinToString(" ")))
    }

    @Test
    fun `abbreviations do not end a sentence`() {
        val units = SentenceSplitter.sentences("Mr. Vance opened the door. Dr. Ellis waited.")
        assertEquals(listOf("Mr. Vance opened the door.", "Dr. Ellis waited."), units)
    }

    @Test
    fun `decimals do not end a sentence`() {
        val units = SentenceSplitter.sentences("It weighed 3.5 kilos. That was all.")
        assertEquals(listOf("It weighed 3.5 kilos.", "That was all."), units)
    }

    @Test
    fun `a quoted question does not split before its attribution`() {
        // The terminator is inside the quotes, but the sentence continues:
        // splitting at the quote would narrate "Who's there?" and "she
        // asked." as two separate utterances with a pause between them.
        val units = SentenceSplitter.sentences("\"Who's there?\" she asked. Nobody answered.")
        assertEquals(
            listOf("\"Who's there?\" she asked.", "Nobody answered."),
            units,
        )
    }

    @Test
    fun `a quoted question does split when a new sentence follows`() {
        val units = SentenceSplitter.sentences("\"Who's there?\" Nobody answered.")
        assertEquals(listOf("\"Who's there?\"", "Nobody answered."), units)
    }

    @Test
    fun `text with no terminal punctuation is still a unit`() {
        assertEquals(listOf("no full stop here"), SentenceSplitter.units("no full stop here"))
    }

    @Test
    fun `the first unit is smaller than the rest`() {
        // This is the whole latency argument: the listener waits for unit
        // one and nothing else, so unit one is kept short.
        val sentence = "This is a sentence of a fairly ordinary length. "
        val units = SentenceSplitter.units(sentence.repeat(20))
        assertTrue("expected several units, got ${units.size}", units.size > 2)
        assertTrue(
            "first unit ${units[0].length} should be <= FIRST_UNIT_CHARS + one sentence",
            units[0].length <= SentenceSplitter.FIRST_UNIT_CHARS + sentence.length,
        )
    }

    @Test
    fun `a fifty thousand word story splits without loss`() {
        // The size the user asked about. Conservation has to hold here too,
        // and the unit count has to stay proportional rather than collapsing
        // to one enormous unit.
        val text = ("The quick brown fox jumped over the lazy dog. ").repeat(5_000)
        val units = SentenceSplitter.units(text)
        assertTrue("expected many units, got ${units.size}", units.size > 1_000)
        assertEquals(normalise(text), normalise(units.joinToString(" ")))
    }

    @Test
    fun `empty input produces no units`() {
        assertEquals(emptyList<String>(), SentenceSplitter.units(""))
        assertEquals(emptyList<String>(), SentenceSplitter.units("   \n  "))
    }
}
