package dev.ahmedmohamed.hayaitts.domain.model

/**
 * Chips on the "Choose a Narrator" step.
 *
 * Each chip maps to a predicate over data the voice actually carries. The
 * PodC mockup also shows "Youth" and "Narrator" chips; neither has a signal
 * in the catalog — there is no age field, and `recommendedUseCases` is absent
 * on most voices — so rather than ship chips that filter nothing, the set
 * follows the one attribute every speaker does declare.
 */
enum class NarratorFilter {
    ALL,
    FEMALE,
    MALE,
    NEUTRAL,
    ;

    fun matches(voice: InstalledVoice): Boolean = when (this) {
        ALL -> true
        FEMALE -> voice.hasSpeakerOf(Gender.FEMALE)
        MALE -> voice.hasSpeakerOf(Gender.MALE)
        NEUTRAL -> voice.hasSpeakerOf(Gender.NEUTRAL)
    }

    private fun InstalledVoice.hasSpeakerOf(gender: Gender): Boolean =
        speakers.any { Gender.parse(it.gender) == gender }
}

/** Applies [chip] to the receiver, preserving order. */
fun List<InstalledVoice>.filteredBy(chip: NarratorFilter): List<InstalledVoice> =
    filter { voice -> chip.matches(voice) }
