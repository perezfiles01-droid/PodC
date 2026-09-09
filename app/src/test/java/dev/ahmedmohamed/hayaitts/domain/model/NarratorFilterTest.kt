package dev.ahmedmohamed.hayaitts.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every chip on the narrator step must map to a predicate that actually
 * discriminates. The failure this guards against is a chip added later
 * pointing at a field the voice does not carry: it would render, filter
 * nothing, and look like it worked.
 *
 * The chip set is enumerated from the enum at runtime, so a new one is
 * covered without editing this test's machinery.
 */
class NarratorFilterTest {

    private fun voice(id: String, vararg genders: String) = InstalledVoice(
        voiceId = id,
        family = ModelFamily.PIPER,
        title = id,
        languages = listOf("en-US"),
        speakers = genders.mapIndexed { i, g -> Speaker(id = i, name = "s$i", gender = g) },
        sampleRateHz = 22_050,
        installedPath = "/tmp/$id",
        tier = Tier.MID,
        installedAt = 0L,
    )

    private val library = listOf(
        voice("female-only", "F"),
        voice("male-only", "M"),
        voice("neutral-only", "N"),
        voice("mixed", "F", "M"),
        voice("unknown", ""),
    )

    @Test
    fun `all admits everything`() {
        assertEquals(library, library.filteredBy(NarratorFilter.ALL))
    }

    @Test
    fun `gender chips select on any speaker`() {
        assertEquals(
            listOf("female-only", "mixed"),
            library.filteredBy(NarratorFilter.FEMALE).map { it.voiceId },
        )
        assertEquals(
            listOf("male-only", "mixed"),
            library.filteredBy(NarratorFilter.MALE).map { it.voiceId },
        )
        assertEquals(
            listOf("neutral-only"),
            library.filteredBy(NarratorFilter.NEUTRAL).map { it.voiceId },
        )
    }

    @Test
    fun `a voice with no gender signal is not claimed by a gender chip`() {
        NarratorFilter.entries
            .filter { it != NarratorFilter.ALL }
            .forEach { chip ->
                assertTrue(
                    "$chip should not match an unknown-gender voice",
                    library.filteredBy(chip).none { it.voiceId == "unknown" },
                )
            }
    }

    @Test
    fun `every chip discriminates over a real library`() {
        // A chip that matches everything, or nothing, is a chip with no
        // backing data - the exact shape of a filter that silently does not
        // work. ALL is the deliberate exception.
        NarratorFilter.entries
            .filter { it != NarratorFilter.ALL }
            .forEach { chip ->
                val hits = library.filteredBy(chip)
                assertTrue("$chip matched nothing", hits.isNotEmpty())
                assertTrue("$chip matched everything", hits.size < library.size)
            }
    }
}
