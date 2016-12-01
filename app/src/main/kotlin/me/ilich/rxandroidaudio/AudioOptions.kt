package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioTrack

class AudioOptions(
        val encoding: Int,
        val sampleRate: Int,
        val channels: Int
) {

    companion object {
        val PLAYBACK_PCM_8BIT_44100_MONO = AudioOptions(AudioFormat.ENCODING_PCM_8BIT, 44100, AudioFormat.CHANNEL_OUT_MONO)
        val PLAYBACK_PCM_16BIT_44100_MONO = AudioOptions(AudioFormat.ENCODING_PCM_16BIT, 44100, AudioFormat.CHANNEL_OUT_MONO)
        val RECORD_PCM_16BIT_44100_MONO = AudioOptions(AudioFormat.ENCODING_PCM_16BIT, 44100, AudioFormat.CHANNEL_IN_MONO)
    }

    fun bufferSize(): Int = AudioTrack.getMinBufferSize(sampleRate, channels, encoding)

}

