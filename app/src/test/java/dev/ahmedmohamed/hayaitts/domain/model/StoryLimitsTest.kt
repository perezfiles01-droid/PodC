package dev.ahmedmohamed.hayaitts.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Import rules for the Home upload card.
 *
 * These run on the JVM because [StoryLimits] is pure: the rule that decides
 * whether a 6 MB PDF is allowed in should not need a device to assert.
 */
class StoryLimitsTest {

    @Test
    fun `rejects files over the five megabyte cap`() {
        assertEquals(
            StoryLimits.Rejection.TooLarge,
            StoryLimits.rejectionFor("book.txt", StoryLimits.MAX_BYTES + 1),
        )
        assertNull(StoryLimits.rejectionFor("book.txt", StoryLimits.MAX_BYTES))
    }

    @Test
    fun `rejects types outside the allowed extensions`() {
        listOf("scan.pdf", "sheet.xlsx", "clip.mp3", "noextension").forEach { name ->
            assertEquals(
                "$name should be rejected",
                StoryLimits.Rejection.WrongType,
                StoryLimits.rejectionFor(name, 1_024),
            )
        }
        listOf("a.txt", "A.TXT", "notes.md", "raw.text").forEach { name ->
            assertNull("$name should be allowed", StoryLimits.rejectionFor(name, 1_024))
        }
    }

    @Test
    fun `rejects a known-empty file`() {
        assertEquals(StoryLimits.Rejection.Empty, StoryLimits.rejectionFor("book.txt", 0))
    }

    @Test
    fun `an unreported size is not treated as empty`() {
        // Document providers are not obliged to fill in OpenableColumns.SIZE.
        // Reading -1 as "empty" would reject valid documents, which is the
        // bug this assertion exists to catch.
        assertNull(StoryLimits.rejectionFor("book.txt", -1L))
    }

    @Test
    fun `the copied length is checked on both bounds`() {
        assertEquals(StoryLimits.Rejection.Empty, StoryLimits.rejectionForCopied(0))
        assertEquals(StoryLimits.Rejection.Empty, StoryLimits.rejectionForCopied(-1))
        assertEquals(
            StoryLimits.Rejection.TooLarge,
            StoryLimits.rejectionForCopied(StoryLimits.MAX_BYTES + 1),
        )
        assertNull(StoryLimits.rejectionForCopied(1_024))
    }

    @Test
    fun `extension parsing handles dotted names`() {
        assertEquals("txt", StoryLimits.extensionOf("The Midnight Library.v2.txt"))
        assertEquals("", StoryLimits.extensionOf("README"))
        assertEquals("txt", StoryLimits.extensionOf("UPPER.TXT"))
    }
}
