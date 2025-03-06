package miragefairy2024.wave

import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.io.File
import kotlin.math.roundToInt

object GenerateV2 {
    private val logger = getLogger(GenerateV2::class.java)

    private val samplesPerSecond = 48000
    private val pixelsPerSecond = 128

    fun degenerate(inputFile: File, outputFile: File) {
        inputFile
            .readBytes()
            .toWavAsOgg()
            .toWaveformAsWav()
            .also {
                if (it.doubleArray.size > samplesPerSecond * 10) throw RuntimeException("too long: ${inputFile.name} (${it.doubleArray.size.toDouble() / samplesPerSecond.toDouble()}s)")
                logger.info("Waveform Length: ${it.doubleArray.size} samples") // 56256 == 56511 - 255
            }
            .toSpectrogram(8, 1 / 800.0)
            .also {
                logger.info("Image Size: ${it.bufferedImage.width} x ${it.bufferedImage.height}") // 56511 x 129
                // 画像の幅のうち、255は固定の部分に使われる
                // 画像の幅から-255した部分の長さが実際のサンプル数に相当する
                // 画像の幅が+128される度にサンプル数が+48000になってほしい
            }
            .removePhase()
            .let { it.resize((it.bufferedImage.width.toDouble() / samplesPerSecond.toDouble() * pixelsPerSecond.toDouble()).roundToInt(), 128) } // 151 x 128
            .toLogScale()
            .writeTo(outputFile.also { it.mkdirsParentOrThrow() })
    }
}
