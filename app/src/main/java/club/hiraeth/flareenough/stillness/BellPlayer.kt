package club.hiraeth.flareenough.stillness

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Plays the meditation bell.
 *
 * If a bell recording is bundled at res/raw/bell (for example a real singing bowl
 * or bell that is free to redistribute), that is played. Otherwise it falls back to
 * a bell synthesised in code: several inharmonic partials, like the modes of a
 * struck metal bell, each with its own fade, so it rings and shimmers rather than
 * beeping. The synth is self made, so there is nothing to licence.
 *
 * Everything is wrapped so a device that refuses audio never crashes the app.
 */
object BellPlayer {

    private const val SAMPLE_RATE = 44_100

    fun play(context: Context) {
        val appContext = context.applicationContext
        // Prefer a bundled recording if one has been added. Looked up by name so the
        // code compiles and runs whether or not the file is present.
        try {
            val id = appContext.resources.getIdentifier("bell", "raw", appContext.packageName)
            if (id != 0) {
                val player = MediaPlayer.create(appContext, id)
                if (player != null) {
                    player.setOnCompletionListener { it.release() }
                    player.start()
                    return
                }
            }
        } catch (_: Exception) {
            // Fall through to the synthesised bell.
        }
        playSynth()
    }

    private class Partial(val ratio: Double, val amp: Double, val decay: Double)

    // A small hand bell: a high, clear fundamental with just a couple of bright
    // partials that fade quickly, for a light "ting" rather than a deep gong.
    private val partials = listOf(
        Partial(1.00, 1.00, 3.0),
        Partial(2.70, 0.35, 4.4),
        Partial(5.10, 0.14, 6.5),
    )

    private fun playSynth(durationSeconds: Double = 2.4, baseHz: Double = 1320.0) {
        try {
            val samples = synthesise(durationSeconds, baseHz)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.setNotificationMarkerPosition(samples.size)
            track.setPlaybackPositionUpdateListener(
                object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(t: AudioTrack?) {
                        runCatching {
                            t?.stop()
                            t?.release()
                        }
                    }

                    override fun onPeriodicNotification(t: AudioTrack?) {}
                },
            )
            track.play()
        } catch (_: Exception) {
            // If audio is unavailable, stay silent rather than crash.
        }
    }

    private fun synthesise(durationSeconds: Double, baseHz: Double): ShortArray {
        val count = (durationSeconds * SAMPLE_RATE).toInt()
        val samples = ShortArray(count)
        val ampSum = partials.sumOf { it.amp }
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            var value = 0.0
            for (p in partials) {
                value += p.amp * sin(2.0 * PI * baseHz * p.ratio * t) * exp(-p.decay * t)
            }
            // A short attack so the strike is soft, then normalise and set the level.
            val attack = min(1.0, t / 0.004)
            value = value / ampSum * attack * 0.6
            samples[i] = (value * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return samples
    }
}
