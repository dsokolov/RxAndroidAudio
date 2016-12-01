package me.ilich.rxandroidaudio

import me.ilich.rxandroidaudio.InputStreamObservable
import org.junit.Assert
import org.junit.Test
import rx.Observable
import java.io.ByteArrayInputStream

class StreamsTest {

    @Test fun a() {
        val expected = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        Observable.
                create<ByteArray>(InputStreamObservable.create(ByteArrayInputStream(expected), me.ilich.rxaa.AudioOptions.PCM_8BIT_44100_MONO_PLAYBACK)).
                subscribe {
                    Assert.assertArrayEquals(expected, it)
                }
    }

}

