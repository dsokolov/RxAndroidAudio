package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.util.Log
import rx.Subscriber

sealed class PlaybackSubscriber<T>(
        audioOptions: AudioOptions,
        bufferSize: Int
) : Subscriber<T>() {

    companion object {

        @JvmStatic fun <T> create(audioOptions: AudioOptions, bufferSize: Int = audioOptions.bufferSize()): PlaybackSubscriber<T> {
            val result = when (audioOptions.encoding) {
                AudioFormat.ENCODING_PCM_8BIT -> Playback8BitSubscriber(audioOptions, bufferSize)
                AudioFormat.ENCODING_PCM_16BIT -> Playback16BitSubscriber(audioOptions, bufferSize)
                else -> throw IllegalArgumentException("Unknown encoding ${audioOptions.encoding}")
            }
            return result as PlaybackSubscriber<T>
        }

    }

    protected val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, audioOptions.sampleRate,
            audioOptions.channels, audioOptions.encoding, bufferSize, AudioTrack.MODE_STREAM)

    override fun onStart() {
        audioTrack.play()
    }

    override fun onCompleted() {
        audioTrack.stop()
        audioTrack.release()
    }

    override fun onNext(data: T) {
        val w = onPlay(data)
        when (w) {
            AudioRecord.ERROR_INVALID_OPERATION -> throw RuntimeException("A")
            AudioRecord.ERROR_BAD_VALUE -> throw RuntimeException("B")
            AudioRecord.ERROR_DEAD_OBJECT -> throw RuntimeException("C")
            AudioRecord.ERROR -> throw RuntimeException("D")
            else -> request(1)
        }
    }

    override fun onError(e: Throwable) {
        audioTrack.stop()
        audioTrack.release()
    }

    protected abstract fun onPlay(data: T): Int

    private class Playback8BitSubscriber(audioOptions: AudioOptions, bufferSize: Int) :
            PlaybackSubscriber<ByteArray>(audioOptions, bufferSize) {

        override fun onPlay(data: ByteArray) = audioTrack.write(data, 0, data.size)

    }

    private class Playback16BitSubscriber(audioOptions: AudioOptions, bufferSize: Int) :
            PlaybackSubscriber<ShortArray>(audioOptions, bufferSize) {

        override fun onPlay(data: ShortArray): Int {
            Log.v("Sokolov", "play ${data.size}")
            return audioTrack.write(data, 0, data.size)
        }

    }

}
