package miragefairy2024.wave

import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.io.File
import kotlin.math.roundToInt

object GenerateV2 {
    private val logger = getLogger(GenerateV2::class.java)

    private val samplesPerSecond = 48000
    private val pixelsPerSecond = 128
    private val bits = 8
    private val amplifier = 1600.0

    private val saveImageHeight = 256 / 2
    private val internalImageHeight = 256 / 2 + 1

    fun degenerate(inputFile: File, outputFile: File) {
        inputFile
            .readBytes()
            .toWavAsOgg()
            .toWaveformAsWav()
            .also {
                if (it.doubleArray.size > samplesPerSecond * 10) throw RuntimeException("too long: ${inputFile.name} (${it.doubleArray.size.toDouble() / samplesPerSecond.toDouble()}s)")
                logger.info("Waveform Length: ${it.doubleArray.size} samples") // 56256 == 56511 - 255
            }
            .toSpectrogram(bits, 1 / amplifier)
            .also {
                logger.info("Image Size: ${it.bufferedImage.width} x ${it.bufferedImage.height}") // 56511 x 129
                // 画像の幅のうち、255は固定の部分に使われる
                // 画像の幅から-255した部分の長さが実際のサンプル数に相当する
                // 画像の幅が+128される度にサンプル数が+48000になってほしい
            }
            .removePhase()
            .let { it.resize((it.bufferedImage.width.toDouble() / samplesPerSecond.toDouble() * pixelsPerSecond.toDouble()).roundToInt(), saveImageHeight) } // 151 x 128
            .toLogScale()
            .writeTo(outputFile.also { it.mkdirsParentOrThrow() })
    }

    fun generate(inputFile: File, outputFile: File) {
        inputFile
            .readSpectrogram()
            .fromLogScale()
            .let { it.resize((it.bufferedImage.width.toDouble() / pixelsPerSecond.toDouble() * samplesPerSecond.toDouble()).roundToInt(), internalImageHeight) }
            .generatePhase()
            .generatePhaseGriffinLim(5, { it.toWaveform(bits, 1.0) }, { it.toSpectrogram(bits, 1.0) })
            .toWaveform(bits, 1 / amplifier)
            .toWavByteArray()
            .toOggAsWav()
            .writeTo(outputFile.also { it.mkdirsParentOrThrow() })
    }
}
