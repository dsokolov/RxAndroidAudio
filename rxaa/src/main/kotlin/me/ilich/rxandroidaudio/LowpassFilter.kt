package me.ilich.rxandroidaudio

import android.media.AudioFormat

/**
 *  Base on http://www.it1me.com/it-answers?id=34312682&ttl=Reimplement+vDSP_deq22+for+Biquad+IIR+Filter+by+hand
 */
sealed class LowpassFilter<T>(cornerFrequency: Float, Q: Float, audioOptions: AudioOptions, bufferSize: Int) {

    companion object {

        @Suppress("unused") @JvmStatic fun crate8bit(
                cornerFrequency: Float, Q: Float, audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_8BIT)
        ) =
                Lowpass8bitFilter(cornerFrequency, Q, audioOptions, bufferSize)

        @Suppress("unused") @JvmStatic fun crate16bit(
                cornerFrequency: Float, Q: Float, audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_16BIT)
        ) =
                Lowpass16bitFilter(cornerFrequency, Q, audioOptions, bufferSize)

        @Suppress("unused") @JvmStatic fun crateFloat(
                cornerFrequency: Float, Q: Float, audioOptions: AudioOptions,
                bufferSize: Int = audioOptions.bufferSize(AudioFormat.ENCODING_PCM_FLOAT)
        ) =
                LowpassFloatFilter(cornerFrequency, Q, audioOptions, bufferSize)

    }

    private val N = bufferSize
    private var input: FloatArray = FloatArray(N + 2) { 0f }
    private var output: FloatArray = FloatArray(N + 2) { 0f }
    private val coefficients = DoubleArray(5)
    private val samplingRate: Int = audioOptions.sampleRate
    private val floatBufferArray = FloatArray(bufferSize)

    init {
        val Fc = cornerFrequency
        val omega = 2 * Math.PI * Fc / samplingRate
        val omegaS = Math.sin(omega)
        val omegaC = Math.cos(omega)
        val alpha = omegaS / (2 * Q)
        val a0 = 1 + alpha
        val b0 = ((1 - omegaC) / 2) / a0
        val b1 = ((1 - omegaC)) / a0
        val b2 = ((1 - omegaC) / 2) / a0
        val a1 = (-2 * omegaC) / a0
        val a2 = (1 - alpha) / a0
        coefficients[0] = b0
        coefficients[1] = b1
        coefficients[2] = b2
        coefficients[3] = a1
        coefficients[4] = a2
    }

    fun filter(buffer: T): T {
        val floatArray = convertToFloat(buffer, floatBufferArray)
        calculate(floatArray)
        convertFromFloat(buffer, floatArray)
        return buffer
    }

    protected abstract fun convertToFloat(buffer: T, floatBuffer: FloatArray): FloatArray

    protected abstract fun convertFromFloat(buffer: T, floatBuffer: FloatArray)

    private fun calculate(buffer: FloatArray) {
        val inputStride = 1 // hardcoded for now
        val outputStride = 1

        input[0] = input[N]
        input[1] = input[N + 1]
        output[0] = output[N]
        output[1] = output[N + 1]

        // copy the current buffer into input
        buffer.forEachIndexed { i, fl ->
            input[i + 2] = fl
        }

        // Not sure if this is neccessary, just here to duplicate NVDSP behaviour:
        for (i in 2..N + 1) {
            output[i] = 0f
        }

        for (n in 2..N + 1) {
            val sumG = input[(n - 0) * inputStride] * coefficients[0] +
                    input[(n - 1) * inputStride] * coefficients[1] +
                    input[(n - 2) * inputStride] * coefficients[2]
            val sumH = output[(n - 3 + 2) * outputStride] * coefficients[3] +
                    output[(n - 4 + 2) * outputStride] * coefficients[4]

            val filteredFrame = sumG - sumH
            output[n] = filteredFrame.toFloat()
        }

        buffer.forEachIndexed { i, fl ->
            buffer[i] = output[i + 2]
        }
    }

    class Lowpass8bitFilter(cornerFrequency: Float, Q: Float, audioOptions: AudioOptions, bufferSize: Int) :
            LowpassFilter<ByteArray>(cornerFrequency, Q, audioOptions, bufferSize) {

        override fun convertToFloat(buffer: ByteArray, floatBuffer: FloatArray): FloatArray =
                buffer.map { b ->
                    b.toFloat() / Byte.MAX_VALUE
                }.toFloatArray()

        override fun convertFromFloat(buffer: ByteArray, floatBuffer: FloatArray) {
            buffer.forEachIndexed { i, byte ->
                buffer[i] = (floatBuffer[i] * Byte.MAX_VALUE).toByte()
            }
        }

    }

    class Lowpass16bitFilter(cornerFrequency: Float, Q: Float, audioOptions: AudioOptions, bufferSize: Int) :
            LowpassFilter<ShortArray>(cornerFrequency, Q, audioOptions, bufferSize) {

        override fun convertToFloat(buffer: ShortArray, floatBuffer: FloatArray): FloatArray =
                buffer.map { sh ->
                    sh.toFloat() / Short.MAX_VALUE
                }.toFloatArray()


        override fun convertFromFloat(buffer: ShortArray, floatBuffer: FloatArray) {
            buffer.forEachIndexed { i, byte ->
                buffer[i] = (floatBuffer[i] * Short.MAX_VALUE).toShort()
            }
        }

    }

    class LowpassFloatFilter(cornerFrequency: Float, Q: Float, audioOptions: AudioOptions, bufferSize: Int) :
            LowpassFilter<FloatArray>(cornerFrequency, Q, audioOptions, bufferSize) {

        override fun convertToFloat(buffer: FloatArray, floatBuffer: FloatArray): FloatArray = buffer

        override fun convertFromFloat(buffer: FloatArray, floatBuffer: FloatArray) {
        }

    }

}

