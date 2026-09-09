package dev.ahmedmohamed.hayaitts.domain.model

/**
 * Chips on the "Choose a Narrator" step.
 *
 * Each chip maps to a predicate over data the speaker actually carries.
 * Filtering happens per speaker rather than per voice, which is what makes
 * the chips useful on a bundle like Kokoro: its 11 speakers split into
 * female and male sets, where the whole bundle would match both. The
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

    fun matches(option: NarratorOption): Boolean = when (this) {
        ALL -> true
        FEMALE -> option.gender == Gender.FEMALE
        MALE -> option.gender == Gender.MALE
        NEUTRAL -> option.gender == Gender.NEUTRAL
    }
}

/** Applies [chip] to the receiver, preserving order. */
fun List<NarratorOption>.filteredBy(chip: NarratorFilter): List<NarratorOption> =
    filter { option -> chip.matches(option) }
