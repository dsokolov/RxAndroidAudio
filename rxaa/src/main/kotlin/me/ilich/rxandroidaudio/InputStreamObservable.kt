package me.ilich.rxandroidaudio

import android.media.AudioFormat
import rx.Observable
import rx.Subscriber
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream

sealed class InputStreamObservable<T>(
        val inputStream: InputStream,
        val bufferSize: Int
) : Observable.OnSubscribe<T> {

    companion object {

        @JvmStatic fun create8bit(inputStream: InputStream, audioOptions: AudioOptions, bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_8BIT)): InputStreamObservable<ByteArray> =
                InputStream8bitObservable(inputStream, bufferSize)

        @JvmStatic fun create16bit(inputStream: InputStream, audioOptions: AudioOptions, bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_16BIT)): InputStreamObservable<ShortArray> =
                InputStream16bitObservable(inputStream, bufferSize)

    }

    override fun call(subscriber: Subscriber<in T>) {
        try {
            val dataInputStream = DataInputStream(BufferedInputStream(inputStream, bufferSize))
            val buffer = onCreateBuffer()
            while (!subscriber.isUnsubscribed) {
                val readed = onRead(buffer, dataInputStream, subscriber)
                if (readed == -1) {
                    break
                }
            }
            subscriber.onCompleted()
        } catch (t: Throwable) {
            subscriber.onError(t)
        } finally {
            inputStream.close()
        }
    }

    protected abstract fun onCreateBuffer(): T

    protected abstract fun onRead(buffer: T, dataInputStream: DataInputStream, subscriber: Subscriber<in T>): Int


    private class InputStream8bitObservable(inputStream: InputStream, bufferSize: Int) :
            InputStreamObservable<ByteArray>(inputStream, bufferSize) {

        override fun onCreateBuffer() = ByteArray(bufferSize)

        override fun onRead(buffer: ByteArray, dataInputStream: DataInputStream, subscriber: Subscriber<in ByteArray>): Int {
            var readed = -1
            while (readed < bufferSize - 1) {
                try {
                    val b = dataInputStream.readByte()
                    readed++
                    buffer[readed] = b
                } catch (e: EOFException) {
                    break
                }
            }
            if (readed != -1) {
                val array = if (readed == bufferSize - 1) {
                    buffer
                } else {
                    buffer.copyOfRange(0, readed + 1)
                }
                subscriber.onNext(array)
            }
            return readed
        }
    }

    private class InputStream16bitObservable(inputStream: InputStream, bufferSize: Int) :
            InputStreamObservable<ShortArray>(inputStream, bufferSize) {

        override fun onCreateBuffer() = ShortArray(bufferSize)

        override fun onRead(buffer: ShortArray, dataInputStream: DataInputStream, subscriber: Subscriber<in ShortArray>): Int {
            var readed = -1
            while (readed < bufferSize - 1) {
                try {
                    val b = dataInputStream.readShort()
                    readed++
                    buffer[readed] = b
                } catch (e: EOFException) {
                    break
                }
            }
            if (readed != -1) {
                val array = if (readed == bufferSize - 1) {
                    buffer
                } else {
                    buffer.copyOfRange(0, readed + 1)
                }
                subscriber.onNext(array)
            }
            return readed
        }

    }

}

