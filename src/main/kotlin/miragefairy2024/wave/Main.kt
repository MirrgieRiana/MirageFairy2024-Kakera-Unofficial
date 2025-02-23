package miragefairy2024.wave

import mirrg.kotlin.slf4j.hydrogen.getLogger
import java.io.File

object Main

private val logger = getLogger(Main::class.java)

fun main() {

    //convert(File("""./src/main/resources/assets/miragefairy2024/sounds/magic_hit.8b.1000ms.8000a.scr.png"""))

    //File("""./src/main/resources/assets/miragefairy2024/sounds""").listFiles()?.forEach { inputFile ->
    //    if (inputFile.name.endsWith(".scr.png")) convert(inputFile)
    //}

    if (false) {
        File("C:\\Users\\tacti\\AppData\\Roaming\\.minecraft\\assets\\out_2\\minecraft__sounds__block__cauldron__dye1.ogg")
            .readBytes()
            .toWavAsOgg()
            .toWaveformAsWav()
            .also { println(it.doubleArray.size) }
            .toSpectrogram(8, 1 / 800.0)
            .also { println("${it.bufferedImage.width} x ${it.bufferedImage.height}") }
            .removePhase()
            .resize(2000, 2000)
            .writeTo(File("./build/tmp.png"))
    }

    if (true) {
        File("./build/tmp.png")
            .readSpectrogram()
            .resize(56511, 129)
            .generatePhaseSimple()
            .generatePhaseGriffinLim(5, { it.toWaveform(8, 1.0) }, { it.toSpectrogram(8, 1.0) })
            .toWaveform(8, 1 / 800.0)
            .toWavByteArray()
            .toOggAsWav()
            .writeTo(File("./build/tmp3.ogg"))
    }

}

fun convert(inputFile: File) {
    logger.info("inputFile    = $inputFile")

    when (inputFile.extension) {
        "wav" -> {
            inputFile
                .readBytes()
                .toWaveformAsWav()
                .toSpectrogram(8, 1 / 4000.0)
                .writeTo(inputFile.resolveSibling("${inputFile.nameWithoutExtension}.png"))
        }

        "png" -> {
            val m = """(.*)\.(\d+)b\.(\d+)ms\.(\d+)a\.scr\.png""".toRegex().matchEntire(inputFile.name)!!


            val baseName = m.groups[1]?.value!!
            logger.info("baseName     = $baseName")
            val outputFile = inputFile.resolveSibling("$baseName.ogg")
            logger.info("outputFile   = $outputFile")

            val bits = m.groups[2]?.value!!.toInt()
            logger.info("bits         = $bits")
            val windowSize = 1 shl bits
            logger.info("windowSize   = $windowSize")
            val imageHeight = windowSize / 2 + 1
            logger.info("imageHeight  = $imageHeight")

            val samplingRate = 48000
            logger.info("samplingRate = $samplingRate")

            val duration = m.groups[3]?.value!!.toDouble() * 0.001
            logger.info("duration     = $duration")
            val imageWidth = (samplingRate * duration + (windowSize - 1)).toInt()
            logger.info("imageWidth   = $imageWidth")

            //val amplifier = 4000.0
            val amplifier = m.groups[4]?.value!!.toDouble()
            logger.info("amplifier    = $amplifier")

            inputFile
                .readSpectrogram()
                .resize(imageWidth, imageHeight)
                .generatePhaseSimple()
                .generatePhaseGriffinLim(20, { it.toWaveform(bits, 1.0) }, { it.toSpectrogram(bits, 1.0) })
                .also { it.writeTo(inputFile.resolveSibling("dump.png")) }
                .toWaveform(bits, 1 / amplifier)
                .toWavByteArray()
                .also { it.writeTo(inputFile.resolveSibling("dump.wav")) }
                .toOggAsWav()
                .writeTo(outputFile)
        }
    }
}
