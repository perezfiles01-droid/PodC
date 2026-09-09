package dev.ahmedmohamed.hayaitts.data.catalog

import dev.ahmedmohamed.hayaitts.domain.model.CatalogManifest
import dev.ahmedmohamed.hayaitts.domain.model.englishOnly
import dev.ahmedmohamed.hayaitts.domain.model.isEnglishTag
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PodC ships English narration only. Two things can put other languages
 * back, and both are asserted here:
 *
 *  1. the bundled manifest — enumerated voice by voice from the real file,
 *     never a sampled or hardcoded list, so a voice added by the weekly
 *     catalog-refresh workflow is covered the moment it lands;
 *  2. the network refresh — the upstream manifest is still multilingual, so
 *     the gate itself is exercised against a mixed payload. Pruning the
 *     bundled file alone would leave this path wide open.
 */
class CatalogEnglishOnlyTest {

    /** Unit tests run with `app/` as the working directory. */
    private val manifestFile = File("../catalog/v1/models.json")

    private fun manifest(): CatalogManifest {
        assertTrue(
            "Bundled catalog not found at ${manifestFile.absolutePath}",
            manifestFile.isFile,
        )
        return catalogJson.decodeFromString(manifestFile.readText())
    }

    @Test
    fun `every voice in the bundled catalog speaks English`() {
        val voices = manifest().voices
        assertTrue("Bundled catalog is empty", voices.isNotEmpty())
        val offenders = voices
            .filterNot { card -> card.languages.any(::isEnglishTag) }
            .map { "${it.id} ${it.languages}" }
        assertEquals("Non-English voices in the bundled catalog", emptyList<String>(), offenders)
    }

    @Test
    fun `the gate drops non-English voices from a mixed payload`() {
        // Stands in for the upstream manifest the background refresh pulls.
        val mixed = manifest().voices
        val gated = mixed.englishOnly()
        assertEquals(mixed.size, gated.size)

        // And with real non-English shapes mixed in, taken from the tags the
        // upstream catalog actually uses.
        val foreign = listOf("de-DE", "zh", "cy", "gb", "uk", "ua", "fa-IR")
        val sample = mixed.first()
        val planted = foreign.mapIndexed { i, tag ->
            sample.copy(id = "planted-$i", languages = listOf(tag))
        }
        assertEquals(mixed.size, (mixed + planted).englishOnly().size)
    }

    @Test
    fun `tag matching is on the primary subtag`() {
        // The catalog splits `en_US` into ["en", "us"], so a region fragment
        // never stands alone for an English voice. Prefix matching would
        // wrongly keep Welsh (cy/gb) and drop nothing it should — these
        // near-misses are the shapes that make the difference.
        listOf("en", "en-US", "en_GB", "EN-us", " en ").forEach {
            assertTrue("$it should match", isEnglishTag(it))
        }
        listOf("eng", "enm", "us", "gb", "cy", "uk", "de-DE", "").forEach {
            assertFalse("$it should not match", isEnglishTag(it))
        }
    }
}
