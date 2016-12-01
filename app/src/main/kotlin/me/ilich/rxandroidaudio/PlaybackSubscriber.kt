package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
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

    override fun onError(e: Throwable) {
        audioTrack.stop()
        audioTrack.release()
    }

    private class Playback8BitSubscriber(audioOptions: AudioOptions, bufferSize: Int) :
            PlaybackSubscriber<ByteArray>(audioOptions, bufferSize) {

        override fun onNext(data: ByteArray) {
            audioTrack.write(data, 0, data.size)
        }

    }

    private class Playback16BitSubscriber(audioOptions: AudioOptions, bufferSize: Int) :
            PlaybackSubscriber<ShortArray>(audioOptions, bufferSize) {

        override fun onNext(data: ShortArray) {
            audioTrack.write(data, 0, data.size)
        }

    }

}
