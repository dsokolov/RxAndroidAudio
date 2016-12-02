package me.ilich.rxandroidaudio

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioLevelTest {

    @Test fun rms() {
        assertEquals(0.0, AudioLevel.rms(shortArrayOf()), 0.01)
        assertEquals(0.0, AudioLevel.rms(shortArrayOf(0, 0, 0, 0, 0)), 0.01)
        assertEquals(1.0, AudioLevel.rms(shortArrayOf(1, 1, 1, 1, 1)), 0.01)
        assertEquals(1.0, AudioLevel.rms(shortArrayOf(-1, -1, -1, -1, -1)), 0.01)

        assertEquals(Short.MAX_VALUE.toDouble(), AudioLevel.rms(shortArrayOf(Short.MAX_VALUE)), 0.01)
        assertEquals(Short.MAX_VALUE.toDouble(), AudioLevel.rms(shortArrayOf(Short.MAX_VALUE, Short.MAX_VALUE)), 0.01)
        assertEquals(Short.MAX_VALUE.toDouble(), AudioLevel.rms(shortArrayOf(Short.MAX_VALUE, Short.MAX_VALUE, Short.MAX_VALUE)), 1.01)

        assertEquals(Short.MAX_VALUE.toDouble(), AudioLevel.rms(shortArrayOf(Short.MIN_VALUE, Short.MIN_VALUE, Short.MIN_VALUE)), 1.01)

        assertEquals(5.5226, AudioLevel.rms(shortArrayOf(5, 6)), 0.01)
        assertEquals(10.0, AudioLevel.rms(shortArrayOf(-10, 10)), 0.01)
    }

}