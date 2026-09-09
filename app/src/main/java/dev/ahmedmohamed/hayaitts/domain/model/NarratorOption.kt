package dev.ahmedmohamed.hayaitts.domain.model

/**
 * One selectable narrator: a **speaker**, not a voice.
 *
 * A multi-speaker bundle like `kokoro-en-v0_19` carries 11 distinct voices
 * behind one model, and offering the bundle as a single row both hid ten of
 * them and made the choice meaningless — the player synthesized speaker 0
 * whatever the user picked. Flattening to one row per speaker makes the sid
 * an explicit part of the selection that travels all the way to synthesis.
 */
data class NarratorOption(
    val voiceId: String,
    val sid: Int,
    /** Voice title, e.g. "Kokoro (11 speakers)". */
    val voiceTitle: String,
    /** Speaker name from the bundle, e.g. "af_bella". */
    val speakerName: String,
    val gender: Gender,
    val languages: List<String>,
) {
    /**
     * What the row reads. Single-speaker voices show the voice title alone —
     * "Ljspeech" rather than "Ljspeech · speaker_0".
     */
    val label: String
        get() = if (speakerName.isBlank()) voiceTitle else speakerName

    val subtitle: String
        get() = buildString {
            append(languages.firstOrNull().orEmpty())
            if (voiceTitle.isNotBlank() && speakerName.isNotBlank()) {
                if (isNotEmpty()) append("  ·  ")
                append(voiceTitle)
            }
        }
}

/**
 * Flattens installed voices into one option per speaker, preserving order.
 * A voice with no speaker metadata still yields exactly one option, so the
 * list is never empty for an installed voice.
 */
fun List<InstalledVoice>.toNarratorOptions(): List<NarratorOption> = flatMap { voice ->
    if (voice.speakers.isEmpty()) {
        listOf(
            NarratorOption(
                voiceId = voice.voiceId,
                sid = 0,
                voiceTitle = voice.title,
                speakerName = "",
                gender = Gender.UNKNOWN,
                languages = voice.languages,
            ),
        )
    } else {
        voice.speakers.map { speaker ->
            NarratorOption(
                voiceId = voice.voiceId,
                sid = speaker.id,
                voiceTitle = voice.title,
                // A single-speaker bundle's lone placeholder name adds
                // nothing next to the voice title.
                speakerName = if (voice.speakers.size == 1) "" else speaker.name,
                gender = Gender.parse(speaker.gender),
                languages = voice.languages,
            )
        }
    }
}
