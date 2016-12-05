package me.ilich.rxandroidaudio

/**
 *  Base on http://www.it1me.com/it-answers?id=34312682&ttl=Reimplement+vDSP_deq22+for+Biquad+IIR+Filter+by+hand
 */
class LowpassFilter(bufferSize: Int, samplingRate: Int, cornerFrequency: Float, Q: Float) {

    private val N = bufferSize
    private var input: FloatArray = FloatArray(N + 2) { 0f }
    private var output: FloatArray = FloatArray(N + 2) { 0f }
    private val coefficients = DoubleArray(5)

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
        coefficients[0] = b0;
        coefficients[1] = b1;
        coefficients[2] = b2;
        coefficients[3] = a1;
        coefficients[4] = a2;
    }

    fun filter(buffer: ByteArray): ByteArray {
        val floatArray = buffer.map { b ->
            b.toFloat() / Byte.MAX_VALUE
        }.toFloatArray()
        calculate(floatArray)
        buffer.forEachIndexed { i, byte ->
            buffer[i] = (floatArray[i] * Byte.MAX_VALUE).toByte()
        }
        return buffer
    }

    fun filter(buffer: ShortArray): ShortArray {
        val floatArray = buffer.map { sh ->
            sh.toFloat() / Short.MAX_VALUE
        }.toFloatArray()
        calculate(floatArray)
        buffer.forEachIndexed { i, sh ->
            buffer[i] = (floatArray[i] * Short.MAX_VALUE).toShort()
        }
        return buffer
    }

    fun filter(buffer: FloatArray): FloatArray {
        calculate(buffer)
        return buffer
    }

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

}

