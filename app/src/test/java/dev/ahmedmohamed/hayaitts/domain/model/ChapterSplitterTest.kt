package dev.ahmedmohamed.hayaitts.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The split feeds both the synthesis chunking and the chapter list, so the
 * invariant that matters most is conservation: every character of the source
 * has to end up in exactly one chapter. A splitter that quietly drops a
 * paragraph produces audio that simply skips part of the book, which is
 * indistinguishable from a bad recording.
 */
class ChapterSplitterTest {

    private val story = """
        Chapter 1

        The library stood at the end of the street, and it was always open.

        Nora had walked past it a hundred times without going in.

        Chapter 2

        Inside, the shelves went further back than the building should allow.
    """.trimIndent()

    @Test
    fun `headings start new chapters`() {
        val chapters = ChapterSplitter.split(story)
        assertEquals(listOf("Chapter 1", "Chapter 2"), chapters.map { it.title })
        assertEquals(listOf(0, 1), chapters.map { it.index })
    }

    @Test
    fun `no source text is dropped or duplicated`() {
        val chapters = ChapterSplitter.split(story)
        val rejoined = chapters.joinToString("\n\n") { it.text }
        val normalise: (String) -> String = { s -> s.replace(Regex("\\s+"), " ").trim() }
        assertEquals(normalise(story), normalise(rejoined))
    }

    @Test
    fun `long prose is chunked at the target size`() {
        val paragraph = "word ".repeat(100).trim()
        val long = List(20) { paragraph }.joinToString("\n\n")
        val chapters = ChapterSplitter.split(long, targetChars = 1_000)
        assertTrue("expected several chunks, got ${chapters.size}", chapters.size > 1)
        // Conservation holds for the chunked path too.
        val rejoined = chapters.joinToString("\n\n") { it.text }
        assertEquals(long, rejoined)
    }

    @Test
    fun `a single oversized paragraph is not cut mid sentence`() {
        val huge = "word ".repeat(2_000).trim()
        val chapters = ChapterSplitter.split(huge, targetChars = 100)
        assertEquals(1, chapters.size)
        assertEquals(huge, chapters.single().text)
    }

    @Test
    fun `empty and whitespace-only input produce no chapters`() {
        assertEquals(emptyList<Chapter>(), ChapterSplitter.split(""))
        assertEquals(emptyList<Chapter>(), ChapterSplitter.split("   \n\n  \n "))
    }

    @Test
    fun `a single line story is one chapter`() {
        val chapters = ChapterSplitter.split("Just one line.")
        assertEquals(1, chapters.size)
        assertEquals("Just one line.", chapters.single().text)
    }
}
