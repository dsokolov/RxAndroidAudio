package me.ilich.rxandroidaudio

import android.media.AudioFormat
import android.util.Log
import rx.Subscriber
import java.io.DataOutputStream
import java.io.OutputStream

sealed class OutputStreamSubscriber<T>(
        private val outputStream: OutputStream,
        audioOptions: AudioOptions
) : Subscriber<T>() {

    companion object {

        @JvmStatic fun <T> create(outputStream: OutputStream, audioOptions: AudioOptions): OutputStreamSubscriber<T> {
            val result = when (audioOptions.encoding) {
                AudioFormat.ENCODING_PCM_8BIT -> OutputStream8bitSubscriber(outputStream, audioOptions)
                AudioFormat.ENCODING_PCM_16BIT -> OutputStream16bitSubscriber(outputStream, audioOptions)
                else -> throw IllegalArgumentException("Unknown encoding ${audioOptions.encoding}")
            }
            return result as OutputStreamSubscriber<T>
        }

    }

    private val dataOutputStream = DataOutputStream(outputStream)

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

    private class OutputStream8bitSubscriber(outputStream: OutputStream, audioOptions: AudioOptions) :
            OutputStreamSubscriber<ByteArray>(outputStream, audioOptions) {

        override fun onWrite(dataOutputStream: DataOutputStream, data: ByteArray) {
            data.forEach {
                dataOutputStream.writeByte(it.toInt())
            }
        }
    }

    private class OutputStream16bitSubscriber(outputStream: OutputStream, audioOptions: AudioOptions) :
            OutputStreamSubscriber<ShortArray>(outputStream, audioOptions) {

        override fun onWrite(dataOutputStream: DataOutputStream, data: ShortArray) {
            Log.v("Sokolov", "write ${data.size}")
            data.forEach {
                dataOutputStream.writeShort(it.toInt())
            }
        }

    }

}
