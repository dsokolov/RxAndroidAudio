package me.ilich.rxandroidaudio

import android.media.AudioFormat
import rx.Observable
import rx.Subscriber

sealed class ToneObservable<T>(
        val frequency: Double,
        val audioOptions: AudioOptions,
        val bufferSize: Int
) : Observable.OnSubscribe<T> {

    companion object {

        private const val TwoPi = 2 * Math.PI

        @JvmStatic fun <T> create8bit(frequency: Double, audioOptions: AudioOptions, bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_8BIT)): ToneObservable<ByteArray> =
                Tone8bitObservable(frequency, audioOptions, bufferSize)

        @JvmStatic fun <T> create816it(frequency: Double, audioOptions: AudioOptions, bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_16BIT)): ToneObservable<ShortArray> =
                Tone16bitObservable(frequency, audioOptions, bufferSize)

    }

    private val freqPerSr = frequency / audioOptions.sampleRate
    private val phChange = TwoPi * freqPerSr

    override fun call(subscriber: Subscriber<in T>) {
        val samples = onCreateSamples()
        val amp: Short = 10000

        var ph = Math.PI
        for (i in 0..bufferSize - 1) {
            val s = amp * Math.sin(ph)
            if (s > 0.0) {
                onPutMaxSample(samples, i)
            }
            if (s < 0.0) {
                onPutMinSample(samples, i)
            }
            ph += phChange
        }
        while (!subscriber.isUnsubscribed) {
            onNext(subscriber, samples)
        }
        subscriber.onCompleted()
    }

    protected abstract fun onCreateSamples(): T

    protected abstract fun onPutMinSample(samples: T, index: Int)

    protected abstract fun onPutMaxSample(samples: T, index: Int)

    protected abstract fun onNext(subscriber: Subscriber<in T>, data: T)


    private class Tone8bitObservable(frequency: Double, audioOptions: AudioOptions, bufferSize: Int) :
            ToneObservable<ByteArray>(frequency, audioOptions, bufferSize) {

        override fun onCreateSamples() = ByteArray(bufferSize)

        override fun onPutMinSample(samples: ByteArray, index: Int) {
            samples[index] = Byte.MIN_VALUE
        }

        override fun onPutMaxSample(samples: ByteArray, index: Int) {
            samples[index] = Byte.MAX_VALUE
        }

        override fun onNext(subscriber: Subscriber<in ByteArray>, data: ByteArray) {
            subscriber.onNext(data)
        }

    }

    private class Tone16bitObservable(frequency: Double, audioOptions: AudioOptions, bufferSize: Int) :
            ToneObservable<ShortArray>(frequency, audioOptions, bufferSize) {

        override fun onCreateSamples() = ShortArray(bufferSize)

        override fun onPutMinSample(samples: ShortArray, index: Int) {
            samples[index] = Short.MIN_VALUE
        }

        override fun onPutMaxSample(samples: ShortArray, index: Int) {
            samples[index] = Short.MAX_VALUE
        }

        override fun onNext(subscriber: Subscriber<in ShortArray>, data: ShortArray) {
            subscriber.onNext(data)
        }

    }

}

