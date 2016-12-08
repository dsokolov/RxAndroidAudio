package me.ilich.rxandroidaudio

class AudioLevel {

    companion object {

        fun rms(samples: ShortArray) = if (samples.isEmpty()) {
            0.0
        } else {
            Math.sqrt(samples.sumByDouble { (it * it).toDouble() } / samples.size)
        }

        fun maxDecibel(samples: ShortArray): Double =
                samples.
                        map { sh -> sh.toDouble() / Short.MAX_VALUE }.
                        map { db -> 20.0 * Math.log10(db) }.
                        filter(Double::isFinite).
                        max() ?:
                        0.0

    }

}