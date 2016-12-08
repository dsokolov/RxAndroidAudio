package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import rx.Observable
import rx.Subscriber

sealed class RecordObservable<T>(
        val audioOptions: AudioOptions,
        val bufferSize: Int
) : Observable.OnSubscribe<T> {

    companion object {

        @Suppress("unused") @JvmStatic fun create8bit(
                audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_8BIT)
        ): RecordObservable<ByteArray> =
                Record8bitObservable(audioOptions, bufferSize)

        @Suppress("unused") @JvmStatic fun create16bit(
                audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_16BIT)
        ): RecordObservable<ShortArray> =
                Record16bitObservable(audioOptions, bufferSize)

    }

    override fun call(subscriber: Subscriber<in T>) {
        try {
            val encoding = onEncoding()
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT, audioOptions.sampleRate,
                    audioOptions.channels, encoding, bufferSize)
            val state = audioRecord.state
            if (state == AudioRecord.STATE_UNINITIALIZED) {
                subscriber.onError(RuntimeException("STATE_UNINITIALIZED"))
            } else {
                process(audioRecord, subscriber)
            }
        } catch (e: IllegalArgumentException) {
            subscriber.onError(e)
        }
    }

    protected abstract fun onEncoding(): Int

    private fun process(audioRecord: AudioRecord, subscriber: Subscriber<in T>) {
        try {
            audioRecord.startRecording()
            val buffer = onCreateBuffer()
            while (!subscriber.isUnsubscribed) {
                val readed = onReadData(buffer, subscriber, audioRecord)
                when (readed) {
                    AudioRecord.ERROR_INVALID_OPERATION -> throw RuntimeException("A")
                    AudioRecord.ERROR_BAD_VALUE -> throw RuntimeException("B")
                    AudioRecord.ERROR_DEAD_OBJECT -> throw RuntimeException("C")
                    AudioRecord.ERROR -> throw RuntimeException("D")
                }
            }
            subscriber.onCompleted()
        } catch (t: Throwable) {
            subscriber.onError(t)
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    protected abstract fun onCreateBuffer(): T

    protected abstract fun onReadData(buffer: T, subscriber: Subscriber<in T>, audioRecord: AudioRecord): Int

    private class Record8bitObservable(audioOptions: AudioOptions, bufferSize: Int) :
            RecordObservable<ByteArray>(audioOptions, bufferSize) {

        override fun onEncoding(): Int = AudioFormat.ENCODING_PCM_8BIT

        override fun onCreateBuffer() = ByteArray(bufferSize)

        override fun onReadData(buffer: ByteArray, subscriber: Subscriber<in ByteArray>, audioRecord: AudioRecord): Int {
            val readed = audioRecord.read(buffer, 0, bufferSize)
            if (readed > -1) {
                val data = if (readed == bufferSize) {
                    buffer
                } else {
                    buffer.copyOfRange(0, readed)
                }
                subscriber.onNext(data)
            }
            return readed
        }

    }

    private class Record16bitObservable(audioOptions: AudioOptions, bufferSize: Int) :
            RecordObservable<ShortArray>(audioOptions, bufferSize) {

        override fun onEncoding(): Int = AudioFormat.ENCODING_PCM_16BIT

        override fun onCreateBuffer() = ShortArray(bufferSize)

        override fun onReadData(buffer: ShortArray, subscriber: Subscriber<in ShortArray>, audioRecord: AudioRecord): Int {
            val readed = audioRecord.read(buffer, 0, bufferSize)
            if (readed != -1) {
                val data = if (readed == bufferSize) {
                    buffer
                } else {
                    buffer.copyOfRange(0, readed)
                }
                subscriber.onNext(data)
            }
            return readed
        }

    }

}

