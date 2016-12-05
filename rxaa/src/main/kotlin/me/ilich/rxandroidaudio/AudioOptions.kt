package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack

/**
 * Test javadoc
 */
class AudioOptions(
        val mode: Mode,
        val sampleRate: Int,
        val channels: Int
) {

    companion object {

        val PCM_44100_MONO_PLAYBACK = AudioOptions(Mode.PLAYBACK, 44100, AudioFormat.CHANNEL_OUT_MONO)

        val PCM_44100_MONO_RECORD = AudioOptions(Mode.RECORD, 44100, AudioFormat.CHANNEL_IN_MONO)
    }

    enum class Mode {
        PLAYBACK,
        RECORD
    }

    fun bufferSize(encoding: Int): Int = when (mode) {
        Mode.PLAYBACK -> AudioTrack.getMinBufferSize(sampleRate, channels, encoding)
        Mode.RECORD -> AudioRecord.getMinBufferSize(sampleRate, channels, encoding)
    }

}

