package club.hiraeth.flareenough.stillness

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Plays a soft bell tone generated in code. The sound is synthesised here, not
 * shipped as an audio file, so there is nothing to licence and nothing to source.
 * It is a gentle fundamental with two quiet harmonics and a smooth fade, meant to
 * be a calm cue rather than an alarm.
 *
 * Every call is wrapped so a device that refuses audio never crashes the app.
 */
object BellPlayer {

    private const val SAMPLE_RATE = 44_100

    /** Play a single bell. [durationSeconds] shapes how long it rings out. */
    fun play(durationSeconds: Double = 2.0, fundamentalHz: Double = 528.0) {
        try {
            val samples = synthesise(durationSeconds, fundamentalHz)
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

    private fun synthesise(durationSeconds: Double, fundamentalHz: Double): ShortArray {
        val count = (durationSeconds * SAMPLE_RATE).toInt()
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-3.0 * t)
            val wave = sin(2.0 * PI * fundamentalHz * t) +
                0.4 * sin(2.0 * PI * fundamentalHz * 2 * t) +
                0.2 * sin(2.0 * PI * fundamentalHz * 3 * t)
            val value = wave / 1.6 * envelope * 0.6
            samples[i] = (value * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return samples
    }
}
