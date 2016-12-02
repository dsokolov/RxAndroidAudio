package me.ilich.rxandroidaudio

class AudioLevel {

    companion object {

        fun rms(samples: ShortArray) = if (samples.isEmpty()) {
            0.0
        } else {
            Math.sqrt(samples.sumByDouble { (it * it).toDouble() } / samples.size)
        }

        fun db(samples: ShortArray): Double {
            var sum = samples.
                    map { sample -> sample.toDouble() / Short.MAX_VALUE }.
                    sumByDouble { sample -> sample * sample }
            val rms = Math.sqrt(sum / (samples.size));
            val decibel = 20 * Math.log10(rms);
            return decibel
        }

        /*fun measure(samples: ShortArray): Double {
            var maxAudio = -100.0
            samples.forEachIndexed { i, sh ->
                val normalized = (shortToDouble(sh) + 1.0) / 2.0
                val value = Math.log10(normalized) * 20.0
                if (value > maxAudio) {
                    maxAudio = value
                }
            }
            return maxAudio
        }

        fun volumeRMS(raw: DoubleArray): Double {
            var sum = 0.0
            if (raw.isEmpty()) {
                return sum
            } else {
                for (ii in raw.indices) {
                    sum += raw[ii]
                }
            }
            val average = sum / raw.size

            var sumMeanSquare = 0.0
            for (ii in raw.indices) {
                sumMeanSquare += Math.pow(raw[ii] - average, 2.0)
            }
            val averageMeanSquare = sumMeanSquare / raw.size
            val rootMeanSquare = Math.sqrt(averageMeanSquare)

            return rootMeanSquare
        }*/

    }

}

