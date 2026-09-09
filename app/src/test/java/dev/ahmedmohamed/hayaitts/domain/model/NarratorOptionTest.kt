package dev.ahmedmohamed.hayaitts.domain.model

import dev.ahmedmohamed.hayaitts.data.catalog.catalogJson
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A multi-speaker bundle must offer one row per speaker.
 *
 * The fixture is the **real** bundled catalog entry rather than a synthetic
 * one: the 11 Kokoro speakers are the case the user reported, and a
 * hand-written stand-in would not notice if the catalog's shape changed
 * under it.
 */
class NarratorOptionTest {

    private fun installed(card: VoiceCard) = InstalledVoice(
        voiceId = card.id,
        family = ModelFamily.KOKORO,
        title = card.title,
        languages = card.languages,
        speakers = card.speakers,
        sampleRateHz = card.sampleRateHz,
        installedPath = "/tmp/${card.id}",
        tier = Tier.HIGH,
        installedAt = 0L,
    )

    private fun card(id: String): VoiceCard {
        val manifest: CatalogManifest =
            catalogJson.decodeFromString(File("../catalog/v1/models.json").readText())
        return manifest.voices.first { it.id == id }
    }

    @Test
    fun `kokoro contributes one row per speaker`() {
        val kokoro = card("kokoro-en-v0_19")
        assertEquals("catalog no longer lists 11 speakers", 11, kokoro.speakers.size)

        val options = listOf(installed(kokoro)).toNarratorOptions()
        assertEquals(11, options.size)
        assertEquals(
            "every row must carry a distinct sid",
            11,
            options.map { it.sid }.toSet().size,
        )
        assertEquals(kokoro.speakers.map { it.id }, options.map { it.sid })
        assertTrue(
            "rows should be labelled with the speaker name",
            options.all { it.label.isNotBlank() } && options.map { it.label }.toSet().size == 11,
        )
    }

    @Test
    fun `a single-speaker voice yields exactly one row labelled by the voice`() {
        val single = card("vits-piper-en_US-amy-low")
        val options = listOf(installed(single)).toNarratorOptions()
        assertEquals(1, options.size)
        assertEquals(single.title, options.single().label)
        assertEquals(0, options.single().sid)
    }

    @Test
    fun `a voice with no speaker metadata still yields one row`() {
        val bare = InstalledVoice(
            voiceId = "bare",
            family = ModelFamily.PIPER,
            title = "Bare",
            languages = listOf("en-US"),
            speakers = emptyList(),
            sampleRateHz = 22_050,
            installedPath = "/tmp/bare",
            tier = Tier.LOW,
            installedAt = 0L,
        )
        val options = listOf(bare).toNarratorOptions()
        assertEquals(1, options.size)
        assertEquals("Bare", options.single().label)
    }
}
