package me.ilich.rxandroidaudio

class AudioLevel {

    companion object {

        private const val EPSILON = 0.001;

        fun rms(samples: ShortArray) = if (samples.isEmpty()) {
            0.0
        } else {
            Math.sqrt(samples.sumByDouble { (it * it).toDouble() } / samples.size)
        }

        fun maxDecibel(samples: ShortArray): Double =
                samples.
                        map { sh ->
                            sh.toDouble() / Short.MAX_VALUE
                        }.
                        map { db ->
                            if (db == 0.0) {
                                EPSILON
                            } else {
                                Math.abs(db)
                            }
                        }.
                        map { db ->
                            20.0 * Math.log10(db)
                        }.
                        filter(Double::isFinite).
                        max() ?:
                        Double.NEGATIVE_INFINITY

    }

}