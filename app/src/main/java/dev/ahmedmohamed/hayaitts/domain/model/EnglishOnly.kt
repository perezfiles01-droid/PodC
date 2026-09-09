package dev.ahmedmohamed.hayaitts.domain.model

/**
 * PodC ships English narration only, so the catalog is gated to English
 * voices in exactly one place: this file, applied by
 * `CatalogRepositoryImpl` to **both** of its load paths.
 *
 * The gate lives here rather than in the bundled JSON because pruning the
 * asset alone covers one of two sources — the background refresh pulls the
 * upstream manifest, which still carries every language, and would put them
 * back on the next launch. Gating in the repository also means every
 * consumer (Browse, Library, Quick Switch, default-voice resolution) is
 * covered by one change, including consumers not written yet.
 */

/**
 * True when [tag] is an English BCP-47 tag: `en`, or any tag whose primary
 * subtag is `en` (`en-US`, `en-GB`, `en_GB`, `EN-us`).
 *
 * Matching is on the primary subtag rather than a prefix so tags that merely
 * begin with the letters "en" — `enm` (Middle English), a hypothetical
 * `en-hant` style extension is still English, but `eng`, `ent` are not —
 * cannot slip through.
 */
fun isEnglishTag(tag: String): Boolean =
    tag.trim().split('-', '_').firstOrNull()?.equals("en", ignoreCase = true) == true

/** True when the voice speaks at least one English locale. */
fun VoiceCard.isEnglish(): Boolean = languages.any(::isEnglishTag)

/** Drops every voice that speaks no English locale. */
fun List<VoiceCard>.englishOnly(): List<VoiceCard> = filter { it.isEnglish() }
