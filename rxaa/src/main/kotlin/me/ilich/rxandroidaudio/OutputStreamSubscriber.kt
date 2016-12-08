package me.ilich.rxandroidaudio

import android.media.AudioFormat
import rx.Subscriber
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.OutputStream

sealed class OutputStreamSubscriber<T>(
        private val outputStream: OutputStream,
        val audioOptions: AudioOptions,
        bufferSize: Int
) : Subscriber<T>() {

    companion object {

        @Suppress("unused") @JvmStatic fun create8bit(
                outputStream: OutputStream, audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_8BIT)
        ): OutputStreamSubscriber<ByteArray> =
                OutputStream8bitSubscriber(outputStream, audioOptions, bufferSize)

        @Suppress("unused") @JvmStatic fun create16bit(
                outputStream: OutputStream, audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_16BIT)
        ): OutputStreamSubscriber<ShortArray> =
                OutputStream16bitSubscriber(outputStream, audioOptions, bufferSize)

    }

    private val dataOutputStream = DataOutputStream(BufferedOutputStream(outputStream, bufferSize))

    override fun onStart() {

    }

    override fun onNext(data: T) {
        onWrite(dataOutputStream, data)
        request(1)
    }

    override fun onCompleted() {
        outputStream.flush()
        outputStream.close()
    }

    override fun onError(e: Throwable) {
        outputStream.flush()
        outputStream.close()
    }

    protected abstract fun onWrite(dataOutputStream: DataOutputStream, data: T)

    private class OutputStream8bitSubscriber(outputStream: OutputStream, audioOptions: AudioOptions, bufferSize: Int) :
            OutputStreamSubscriber<ByteArray>(outputStream, audioOptions, bufferSize) {

        override fun onWrite(dataOutputStream: DataOutputStream, data: ByteArray) {
            data.forEach {
                dataOutputStream.writeByte(it.toInt())
            }
        }
    }

    private class OutputStream16bitSubscriber(outputStream: OutputStream, audioOptions: AudioOptions, bufferSize: Int) :
            OutputStreamSubscriber<ShortArray>(outputStream, audioOptions, bufferSize) {

        override fun onWrite(dataOutputStream: DataOutputStream, data: ShortArray) {
            data.forEach {
                dataOutputStream.writeShort(it.toInt())
            }
        }

    }

}
