package dev.ahmedmohamed.hayaitts.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Narrator rows are speakers, and the chips filter speakers.
 *
 * The chip set is enumerated from the enum at runtime, so a chip added later
 * is covered without editing this test's machinery. The property asserted is
 * that every chip both matches something and excludes something — a chip
 * backed by a field the data does not carry would fail that, having rendered
 * perfectly well and filtered nothing.
 */
class NarratorFilterTest {

    private fun option(id: String, sid: Int, gender: Gender) = NarratorOption(
        voiceId = id,
        sid = sid,
        voiceTitle = id,
        speakerName = "spk$sid",
        gender = gender,
        languages = listOf("en-US"),
    )

    private val options = listOf(
        option("kokoro", 0, Gender.FEMALE),
        option("kokoro", 1, Gender.FEMALE),
        option("kokoro", 5, Gender.MALE),
        option("other", 0, Gender.NEUTRAL),
        option("mystery", 0, Gender.UNKNOWN),
    )

    @Test
    fun `all admits every speaker`() {
        assertEquals(options, options.filteredBy(NarratorFilter.ALL))
    }

    @Test
    fun `gender chips select individual speakers, not whole voices`() {
        // The point of speaker-level filtering: Kokoro's female and male
        // speakers land in different chips, where the bundle matched both.
        assertEquals(
            listOf(0, 1),
            options.filteredBy(NarratorFilter.FEMALE).map { it.sid },
        )
        assertEquals(
            listOf(5),
            options.filteredBy(NarratorFilter.MALE).map { it.sid },
        )
        assertEquals(1, options.filteredBy(NarratorFilter.NEUTRAL).size)
    }

    @Test
    fun `a speaker with no gender signal is not claimed by a gender chip`() {
        NarratorFilter.entries
            .filter { it != NarratorFilter.ALL }
            .forEach { chip ->
                assertTrue(
                    "$chip should not match an unknown-gender speaker",
                    options.filteredBy(chip).none { it.voiceId == "mystery" },
                )
            }
    }

    @Test
    fun `every chip discriminates`() {
        NarratorFilter.entries
            .filter { it != NarratorFilter.ALL }
            .forEach { chip ->
                val hits = options.filteredBy(chip)
                assertTrue("$chip matched nothing", hits.isNotEmpty())
                assertTrue("$chip matched everything", hits.size < options.size)
            }
    }
}
