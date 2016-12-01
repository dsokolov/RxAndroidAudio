package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack

class AudioOptions(
        val encoding: Int,
        val sampleRate: Int,
        val channels: Int
) {

    companion object {

        val PCM_8BIT_44100_MONO_PLAYBACK = AudioOptions(AudioFormat.ENCODING_PCM_8BIT, 44100, AudioFormat.CHANNEL_OUT_MONO)
        val PCM_16BIT_44100_MONO_PLAYBACK = AudioOptions(AudioFormat.ENCODING_PCM_16BIT, 44100, AudioFormat.CHANNEL_OUT_MONO)

        val PCM_8BIT_44100_MONO_RECORD = AudioOptions(AudioFormat.ENCODING_PCM_8BIT, 44100, AudioFormat.CHANNEL_IN_MONO)
        val PCM_16BIT_44100_MONO_RECORD = AudioOptions(AudioFormat.ENCODING_PCM_16BIT, 44100, AudioFormat.CHANNEL_IN_MONO)
    }

    fun playbackBufferSize(): Int = AudioTrack.getMinBufferSize(sampleRate, channels, encoding)
    fun recordBufferSize(): Int = AudioRecord.getMinBufferSize(sampleRate, channels, encoding)

}

