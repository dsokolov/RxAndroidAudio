package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import rx.Subscriber

sealed class PlaybackSubscriber<T>(
        var audioOptions: AudioOptions,
        var bufferSize: Int
) : Subscriber<T>() {

    companion object {

        @Suppress("unused") @JvmStatic fun create8Bit(
                audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_8BIT)
        ): PlaybackSubscriber<ByteArray> =
                Playback8BitSubscriber(audioOptions, bufferSize)

        @Suppress("unused") @JvmStatic fun create16Bit(
                audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_16BIT)
        ): PlaybackSubscriber<ShortArray> =
                Playback16BitSubscriber(audioOptions, bufferSize)

    }

    protected lateinit var audioTrack: AudioTrack

    override fun onStart() {
        val encoding = onEncoding()
        audioTrack = AudioTrack(android.media.AudioManager.STREAM_MUSIC, audioOptions.sampleRate,
                audioOptions.channels, encoding, bufferSize, AudioTrack.MODE_STREAM)
        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            throw RuntimeException("STATE_INITIALIZED")
        }
        audioTrack.play()
    }

    protected abstract fun onEncoding(): Int

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

    override fun onCompleted() {
        audioTrack.stop()
        audioTrack.release()
    }

    override fun onError(e: Throwable) {
        audioTrack.stop()
        audioTrack.release()
    }

    protected abstract fun onPlay(data: T): Int

    private class Playback8BitSubscriber(audioOptions: AudioOptions, bufferSize: Int) :
            PlaybackSubscriber<ByteArray>(audioOptions, bufferSize) {

        override fun onEncoding(): Int = AudioFormat.ENCODING_PCM_8BIT

        override fun onPlay(data: ByteArray) = audioTrack.write(data, 0, data.size)

    }

    private class Playback16BitSubscriber(audioOptions: AudioOptions, bufferSize: Int) :
            PlaybackSubscriber<ShortArray>(audioOptions, bufferSize) {

        override fun onEncoding(): Int = AudioFormat.ENCODING_PCM_16BIT

        override fun onPlay(data: ShortArray): Int = audioTrack.write(data, 0, data.size)

    }

}
