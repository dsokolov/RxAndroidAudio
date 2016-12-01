package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack

class AudioOptions(
        val mode: Mode,
        val encoding: Int,
        val sampleRate: Int,
        val channels: Int
) {

    companion object {

        val PCM_8BIT_44100_MONO_PLAYBACK = AudioOptions(Mode.PLAYBACK, AudioFormat.ENCODING_PCM_8BIT, 44100, AudioFormat.CHANNEL_OUT_MONO)
        val PCM_16BIT_44100_MONO_PLAYBACK = AudioOptions(Mode.PLAYBACK, AudioFormat.ENCODING_PCM_16BIT, 44100, AudioFormat.CHANNEL_OUT_MONO)

        val PCM_8BIT_44100_MONO_RECORD = AudioOptions(Mode.RECORD, AudioFormat.ENCODING_PCM_8BIT, 44100, AudioFormat.CHANNEL_IN_MONO)
        val PCM_16BIT_44100_MONO_RECORD = AudioOptions(Mode.RECORD, AudioFormat.ENCODING_PCM_16BIT, 44100, AudioFormat.CHANNEL_IN_MONO)
    }

    enum class Mode {
        PLAYBACK,
        RECORD
    }

    fun bufferSize(): Int = when (mode) {
        Mode.PLAYBACK -> AudioTrack.getMinBufferSize(sampleRate, channels, encoding)
        Mode.RECORD -> AudioRecord.getMinBufferSize(sampleRate, channels, encoding)
    }

}

