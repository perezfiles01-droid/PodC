package dev.ahmedmohamed.hayaitts.data.narration

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/**
 * Where the narrator's audio goes. Abstracted so the pipeline's timing can be
 * asserted against a recording sink in tests — an AudioTrack cannot be
 * constructed on the JVM.
 */
interface AudioSink {
    /** Opens the sink. Called once, before any [write]. */
    fun start(sampleRate: Int)

    /** Blocks until [samples] have been accepted. */
    fun write(samples: FloatArray)

    /** Stops and releases. Safe to call twice. */
    fun stop()
}

/**
 * One long-lived streaming [AudioTrack].
 *
 * The player's old path built a track per chapter, sized to the whole
 * chapter's PCM, which meant nothing could be written until everything had
 * been generated. This one is opened once at the first chunk and fed
 * continuously, so audio starts as soon as the first unit lands and
 * continues across unit and chapter boundaries without a gap.
 */
class AudioTrackSink : AudioSink {

    @Volatile
    private var track: AudioTrack? = null

    override fun start(sampleRate: Int) {
        stop()
        val minBytes = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(MIN_BUFFER_BYTES)
        val created = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            // Four times the minimum: enough to ride out a slow unit without
            // an underrun, small enough that pause stops promptly.
            .setBufferSizeInBytes(minBytes * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        created.play()
        track = created
    }

    override fun write(samples: FloatArray) {
        val current = track ?: return
        val pcm = ByteArray(samples.size * 2)
        for (i in samples.indices) {
            val clamped = (samples[i].coerceIn(-1f, 1f) * 32767f).toInt()
            pcm[i * 2] = (clamped and 0xFF).toByte()
            pcm[i * 2 + 1] = ((clamped shr 8) and 0xFF).toByte()
        }
        var offset = 0
        while (offset < pcm.size) {
            // WRITE_BLOCKING is what paces the producer: once the track's
            // buffer is full this call blocks, so synthesis runs exactly as
            // far ahead as the queue allows and no further.
            val written = current.write(pcm, offset, pcm.size - offset, AudioTrack.WRITE_BLOCKING)
            if (written <= 0) return
            offset += written
        }
    }

    override fun stop() {
        val toRelease = track ?: return
        track = null
        runCatching { toRelease.pause() }
        runCatching { toRelease.flush() }
        runCatching { toRelease.stop() }
        runCatching { toRelease.release() }
    }

    private companion object {
        const val MIN_BUFFER_BYTES = 8 * 1024
    }
}
