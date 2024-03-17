package miragefairy2024.wave

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object Wave

private val logger = getLogger(Wave::class.java)

fun File.readWaveform(): DoubleArray {
    AudioSystem.getAudioInputStream(this).use { input ->
        logger.info("${input.format}")
        logger.info("${input.format.sampleRate}")
        logger.info("${input.format.frameSize}")

        val inputBytes = input.readAllBytes()
        val length = inputBytes.size / input.format.frameSize
        val outputDoubles = DoubleArray(length)
        repeat(length) { i ->

            val a = i * input.format.frameSize
            var value = 0.0
            repeat(input.format.channels) { channel ->
                value += when (input.format.sampleSizeInBits) {
                    8 -> inputBytes[a + 1 * channel + 0].toInt().toDouble()

                    16 -> {
                        val b0 = inputBytes[a + 2 * channel + 0]
                        val b1 = inputBytes[a + 2 * channel + 1]
                        ((b0.toInt() and 0xFF) or (b1.toInt() shl 8)).toDouble()
                    }

                    else -> throw RuntimeException("Bits not supported: ${input.format.sampleSizeInBits}")
                }
            }
            value /= input.format.channels

            outputDoubles[i] = value
        }

        return outputDoubles
    }
}

fun File.writeWaveform(waveform: DoubleArray) {

    val bytes = ByteArray(waveform.size * 2)
    repeat(waveform.size) { i ->
        val int = waveform[i].toInt().coerceIn(-32768 until 32768)
        bytes[2 * i + 0] = (int shr 0 and 0xFF).toByte()
        bytes[2 * i + 1] = (int shr 8 and 0xFF).toByte()
    }

    val format = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        48000.0F,
        16,
        1,
        2,
        48000.0F,
        false,
    )
    AudioSystem.write(AudioInputStream(ByteArrayInputStream(bytes), format, waveform.size.toLong()), AudioFileFormat.Type.WAVE, this)
}

fun DoubleArray.writeTo(file: File) = file.writeWaveform(this)

fun ByteArray.writeTo(file: File) = file.writeBytes(this)

fun DoubleArray.toWav(): ByteArray {

    val bytes = ByteArray(this.size * 2)
    repeat(this.size) { i ->
        val int = this[i].toInt().coerceIn(-32768 until 32768)
        bytes[2 * i + 0] = (int shr 0 and 0xFF).toByte()
        bytes[2 * i + 1] = (int shr 8 and 0xFF).toByte()
    }

    val output = ByteArrayOutputStream()

    val format = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        48000.0F,
        16,
        1,
        2,
        48000.0F,
        false,
    )
    AudioSystem.write(AudioInputStream(ByteArrayInputStream(bytes), format, this.size.toLong()), AudioFileFormat.Type.WAVE, output)

    return output.toByteArray()
}

fun ByteArray.wavToOgg(): ByteArray {
    val processBuilder = ProcessBuilder("bash", "-c", "ffmpeg -i - -f ogg -")
    val process = processBuilder.start()
    return runBlocking {
        launch(Dispatchers.IO) {
            process.outputStream.use { output ->
                output.write(this@wavToOgg)
            }
        }
        val err = async(Dispatchers.IO) {
            process.errorStream.use { input ->
                input.readBytes()
            }
        }
        val result = async(Dispatchers.IO) {
            process.inputStream.use { input ->
                input.readBytes()
            }
        }
        val returnCode = process.waitFor()
        if (returnCode != 0) throw IOException("Process exit: $returnCode\n${err.await().toString(Charsets.UTF_8).replace("""\n+\Z""".toRegex(), "")}")
        result.await()
    }
}


fun Array<Complex>.fft(): Array<Complex> {
    val fft = FFT(false)
    fft.data = this
    fft.execute()
    return fft.data
}

fun Array<Complex>.ifft(): Array<Complex> {
    val fft = FFT(true)
    fft.data = this
    fft.execute()
    return fft.data
}


fun hanningWindow(t: Double, length: Double) = 0.5 - 0.5 * cos(2 * PI * t / length)

fun DoubleArray.getSpectrogram(bits: Int, m: Double): BufferedImage {
    check(bits >= 4)
    val windowSize = 1 shl bits
    logger.info("Window Size: $windowSize")
    logger.info("Input Waveform Length: ${this.size}")
    val width = this.size - windowSize + 1
    check(width >= 1)
    val height = windowSize / 2 + 1

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    repeat(width) { x ->

        val complexWindowedSubWaveform = this.sliceArray(x until x + windowSize).mapIndexed { i, it ->
            val w = hanningWindow(i.toDouble(), windowSize.toDouble())
            Complex(it * w, 0.0)
        }.toTypedArray()
        val outputSpectrum = complexWindowedSubWaveform.fft()

        repeat(height) { y ->
            val r = outputSpectrum[y].re * m
            val g = -128.0 + outputSpectrum[y].abs() * m
            val b = outputSpectrum[y].im * m
            val rgb = (r.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 16) or
                (g.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 8) or
                (b.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 0)
            image.setRGB(x, height - 1 - y, rgb)
        }

    }

    return image
}

fun BufferedImage.fromSpectrogram(bits: Int, m: Double): DoubleArray {
    check(bits >= 4) // 8
    val windowSize = 1 shl bits // 256
    check(width >= windowSize)
    check(height == windowSize / 2 + 1) // 129

    val waveforms = mutableListOf<DoubleArray>()
    val zero = Complex(0.0, 0.0)
    repeat(width) { x -> // 0 .. width - 1

        val spectrum = Array(windowSize) { zero } // Complex[256]
        repeat(height) { y -> // 0 .. 128
            val rgb = this.getRGB(x, height - 1 - y) // (y = 128 .. 0)
            val r = ((rgb shr 16 and 0xFF) - 128).toDouble() / m
            //val g = ((rgb shr 8 and 0xFF) - 128).toDouble() / m
            val b = ((rgb shr 0 and 0xFF) - 128).toDouble() / m

            spectrum[y] = Complex(r, b) // [index = 0 .. 128]
            if (y != 0 && y != height - 1) spectrum[windowSize - y] = Complex(r, -b) // [index = 256 .. 128]
            // y = 0 の画素は直流分を表すため、1か所にしかマッピングされない
            // 虚軸はインデックスが逆になると符号が入れ替わり、実軸は同じ値になる
            // y = 0, 128 において、必ず b = 0 になる性質がある
            // y = 128 のとき、普通には complexes[128] に2度代入することになるが、 b = 0 であるため等価となる
        }

        waveforms += spectrum.ifft().map { it.re }.toDoubleArray()
    }

    val length = waveforms.size - windowSize + 1 // width - 255
    val waveform = DoubleArray(length)
    repeat(length) { x ->
        var value = 0.0
        repeat(windowSize) { i -> // 0 .. 255
            val w = hanningWindow(i.toDouble(), windowSize.toDouble())
            value += waveforms[x + i][windowSize - 1 - i] * w
        }
        waveform[x] = value / windowSize * 2
    }

    return waveform
}

fun BufferedImage.generatePhase(): BufferedImage {
    val windowSize = (height - 1) * 2 // 256

    val random = Random(0)

    // 位相の周期性によりウィンドウサイズを波長とするブザー音が発生するのを防ぐための位相の動的攪乱
    val oldPhases = (0 until height).map { 2 * PI * random.nextDouble() }.toMutableList() // <0 ~ 2PI>[129]
    val newPhases = (0 until height).map { 2 * PI * random.nextDouble() }.toMutableList() // <0 ~ 2PI>[129]

    // 「位相の動的攪乱をリセットする」処理の走るタイミング
    // 位相の攪乱のリセットは、ウィンドウサイズごとに行われる
    val phaseGradientResetOffsets = (0 until height).map { random.nextInt(windowSize) } // <0 .. 255>[129]

    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    repeat(width) { x ->
        repeat(height) { imageY -> // height = 129, imageY = 0 .. 128
            val y = height - 1 - imageY // 128 .. 0

            // y = 0 のとき、 w = 0
            // y = 1 のとき、 w = 2PI / 256
            // y = 32 のとき、 w = 2PI / 8
            // y = 64 のとき、 w = 2PI / 4
            // y = 128 のとき、 w = 2PI / 2
            // w = 2PI / 256 * y
            val w = 2.0 * PI / windowSize * y

            val inputRgb = getRGB(x, imageY)
            val g = (inputRgb shr 8 and 0xFF).toDouble()

            // 位相の動的攪乱の変化の度合い（サンプル位置）
            val phaseGradientResetPhase = (x + phaseGradientResetOffsets[y]) % windowSize // 0 .. 255

            // 位相の動的攪乱の変化の度合い（0～1）
            val phaseGradient = phaseGradientResetPhase / windowSize.toDouble() // 0 ~ 1

            // 位相
            val phase = oldPhases[y] * (1 - phaseGradient) + newPhases[y] * phaseGradient // 0 ~ 2PI

            val r = g * cos(phase + w * x)
            val b = g * sin(phase + w * x)
            val outputRgb = (r.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 16) or
                (g.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 8) or
                (b.toInt().coerceIn(-128, 127) + 128 and 0xFF shl 0)

            // 位相の動的攪乱のリセットのタイミングである場合、リセット
            if (phaseGradientResetPhase == 0) {
                oldPhases[y] = newPhases[y]
                newPhases[y] = 2 * PI * random.nextDouble()
            }

            image.setRGB(x, imageY, outputRgb)
        }
    }
    return image
}

fun BufferedImage.resizeHorizontal(imageHeight: Int): BufferedImage {
    val image = BufferedImage(this.width, imageHeight, BufferedImage.TYPE_INT_RGB)
    val rate = this.height / imageHeight.toDouble() // 3 / 4 = 0.75
    repeat(this.width) { x ->
        repeat(imageHeight) { imageY0 -> // 1

            /*
             *             thisY0      thisY1
             * thisY0f         thisY1f
             * 0               1               2               3  thisY
             * |           ====|========       |               |  this    height=3
             * |           |===========|           |           |  image   height=4
             * 0          [1]          2           3           4  imageY
             *             imageY0     imageY1
             * 0           0.75        1.5         2.25        3  thisY
             *
             * thisY = imageY * 0.75 = imageY * (this.height / image.height) = imageY * rate
             */
            val imageY1 = imageY0 + 1 // 2
            val thisY0 = imageY0 * rate // 0.75
            val thisY1 = imageY1 * rate // 1.5
            val thisY0f = thisY0.toInt() // 0
            val thisY1f = thisY1.toInt() // 1

            var count = 0.0
            var sumR = 0.0
            var sumG = 0.0
            var sumB = 0.0

            fun add(c: Double, thisY: Int) {
                val inputRgb = this.getRGB(x, thisY)
                count += c
                sumR += (inputRgb shr 16 and 0xFF) * c
                sumG += (inputRgb shr 8 and 0xFF) * c
                sumB += (inputRgb shr 0 and 0xFF) * c
            }

            if (thisY0f == thisY1f) {
                // 解像度が上がるためimage画素のすべての範囲が単一のthis画素にマッピングされる場合
                /*
                 *                         thisY0      thisY1
                 *                     thisY0f
                 *                     thisY1f
                 * 0                   1                   2                   3  thisY
                 * |                   |   =============   |                   |  this    height=3
                 * |           |           |===========|           |           |  image   height=5
                 * 0           1          [2]          3           4           5  imageY
                 *                         imageY0     imageY1
                 */
                add(thisY1 - thisY0, thisY0f)
            } else {
                // 対応するthisの画素が始端・0ピクセル以上の中間・終端に分かれる場合
                /*
                 *                     thisY0              thisY1
                 *             thisY0f                 thisY1f
                 * 0           1           2           3           4           5  thisY
                 * |           |       ====|===========|====       |           |  this    height=5
                 * |                   |===================|                   |  image   height=3
                 * 0                  [1]                  2                   3  imageY
                 *                     imageY0             imageY1
                 */
                add((thisY0f + 1) - thisY0, thisY0f)
                @Suppress("ReplaceRangeToWithUntil")
                (thisY0f + 1..thisY1f - 1).forEach { thisYf ->
                    add(1.0, thisYf)
                }
                if (thisY1f < height) add(thisY1 - thisY1f, thisY1f)
            }

            val r = sumR / count
            val g = sumG / count
            val b = sumB / count
            val outputRgb = (r.toInt().coerceIn(0, 255) shl 16) or
                (g.toInt().coerceIn(0, 255) shl 8) or
                (b.toInt().coerceIn(0, 255) shl 0)

            image.setRGB(x, imageY0, outputRgb)
        }
    }
    return image
}

fun BufferedImage.resizeVertical(imageWidth: Int): BufferedImage {
    val image = BufferedImage(imageWidth, this.height, BufferedImage.TYPE_INT_RGB)
    val rate = this.width / imageWidth.toDouble() // 3 / 4 = 0.75
    repeat(this.height) { y ->
        repeat(imageWidth) { imageX0 -> // 1

            /*
             *             thisX0      thisX1
             * thisX0f         thisX1f
             * 0               1               2               3  thisX
             * |           ====|========       |               |  this    height=3
             * |           |===========|           |           |  image   height=4
             * 0          [1]          2           3           4  imageX
             *             imageX0     imageX1
             * 0           0.75        1.5         2.25        3  thisX
             *
             * thisX = imageX * 0.75 = imageX * (this.height / image.height) = imageX * rate
             */
            val imageX1 = imageX0 + 1 // 2
            val thisX0 = imageX0 * rate // 0.75
            val thisX1 = imageX1 * rate // 1.5
            val thisX0f = thisX0.toInt() // 0
            val thisX1f = thisX1.toInt() // 1

            var count = 0.0
            var sumR = 0.0
            var sumG = 0.0
            var sumB = 0.0

            fun add(c: Double, thisX: Int) {
                val inputRgb = this.getRGB(thisX, y)
                count += c
                sumR += (inputRgb shr 16 and 0xFF) * c
                sumG += (inputRgb shr 8 and 0xFF) * c
                sumB += (inputRgb shr 0 and 0xFF) * c
            }

            if (thisX0f == thisX1f) {
                // 解像度が上がるためimage画素のすべての範囲が単一のthis画素にマッピングされる場合
                /*
                 *                         thisX0      thisX1
                 *                     thisX0f
                 *                     thisX1f
                 * 0                   1                   2                   3  thisX
                 * |                   |   =============   |                   |  this    height=3
                 * |           |           |===========|           |           |  image   height=5
                 * 0           1          [2]          3           4           5  imageX
                 *                         imageX0     imageX1
                 */
                add(thisX1 - thisX0, thisX0f)
            } else {
                // 対応するthisの画素が始端・0ピクセル以上の中間・終端に分かれる場合
                /*
                 *                     thisX0              thisX1
                 *             thisX0f                 thisX1f
                 * 0           1           2           3           4           5  thisX
                 * |           |       ====|===========|====       |           |  this    height=5
                 * |                   |===================|                   |  image   height=3
                 * 0                  [1]                  2                   3  imageX
                 *                     imageX0             imageX1
                 */
                add((thisX0f + 1) - thisX0, thisX0f)
                @Suppress("ReplaceRangeToWithUntil")
                (thisX0f + 1..thisX1f - 1).forEach { thisXf ->
                    add(1.0, thisXf)
                }
                if (thisX1f < width) add(thisX1 - thisX1f, thisX1f)
            }

            val r = sumR / count
            val g = sumG / count
            val b = sumB / count
            val outputRgb = (r.toInt().coerceIn(0, 255) shl 16) or
                (g.toInt().coerceIn(0, 255) shl 8) or
                (b.toInt().coerceIn(0, 255) shl 0)

            image.setRGB(imageX0, y, outputRgb)
        }
    }
    return image
}

fun BufferedImage.resize(imageWidth: Int, imageHeight: Int) = this.resizeHorizontal(imageHeight).resizeVertical(imageWidth)


fun File.readImage(): BufferedImage = ImageIO.read(this)

fun File.writeImage(image: BufferedImage) {
    ImageIO.write(image, "png", this)
}

fun BufferedImage.writeTo(file: File) = file.writeImage(this)
