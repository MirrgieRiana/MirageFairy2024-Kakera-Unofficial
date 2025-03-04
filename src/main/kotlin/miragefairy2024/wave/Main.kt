package miragefairy2024.wave

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirrg.kotlin.gson.hydrogen.toJsonElement
import mirrg.kotlin.gson.hydrogen.toJsonWrapper
import mirrg.kotlin.slf4j.hydrogen.getFileLogger
import java.awt.Dimension
import java.io.File
import javax.swing.JButton
import javax.swing.JFrame
import kotlin.math.roundToInt

private val logger = getFileLogger(object {})

private val samplesPerSecond = 48000

object GenerateMain {
    @JvmStatic
    fun main(args: Array<String>) {

        convert(File("""./src/main/resources/assets/miragefairy2024/sounds/magic_hit.8b.1000ms.8000a.scr.png"""))

        //File("""./src/main/resources/assets/miragefairy2024/sounds""").listFiles()?.forEach { inputFile ->
        //    if (inputFile.name.endsWith(".scr.png")) convert(inputFile)
        //}

    }

    fun convert(inputFile: File) {
        logger.info("inputFile        = $inputFile")

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
                logger.info("baseName         = $baseName")
                val outputFile = inputFile.resolveSibling("$baseName.ogg")
                logger.info("outputFile       = $outputFile")

                val bits = m.groups[2]?.value!!.toInt()
                logger.info("bits             = $bits")
                val windowSize = 1 shl bits
                logger.info("windowSize       = $windowSize")
                val imageHeight = windowSize / 2 + 1
                logger.info("imageHeight      = $imageHeight")

                logger.info("samplesPerSecond = $samplesPerSecond")

                val duration = m.groups[3]?.value!!.toDouble() * 0.001
                logger.info("duration         = $duration")
                val imageWidth = (samplesPerSecond.toDouble() * duration + (windowSize - 1)).toInt()
                logger.info("imageWidth       = $imageWidth")

                //val amplifier = 4000.0
                val amplifier = m.groups[4]?.value!!.toDouble()
                logger.info("amplifier        = $amplifier")

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
}

private val pixelsPerSecond = 128

object GenerateV2Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val tasks = mutableListOf<() -> Unit>()

        //tasks += { generateV2("./src/main/resources/assets/miragefairy2024/sounds/entity_chaos_cube_ambient_1") }
        //tasks += { generateV2("./src/main/resources/assets/miragefairy2024/sounds/entity_chaos_cube_ambient_2") }
        //tasks += { generateV2("./src/main/resources/assets/miragefairy2024/sounds/entity_chaos_cube_hurt_1") }
        //tasks += { generateV2("./src/main/resources/assets/miragefairy2024/sounds/entity_chaos_cube_death_1") }
        //tasks += { generateV2("./src/main/resources/assets/miragefairy2024/sounds/004") }
        tasks += { generateV2("./src/main/resources/assets/miragefairy2024/sounds/005") }

        runBlocking {
            tasks.forEach { task ->
                launch(Dispatchers.Default) {
                    task()
                }
            }
        }
    }

    fun generateV2(baseName: String) {
        File("$baseName.scr.png")
            .readSpectrogram()
            .also { logger.info("${it.bufferedImage.width}x${it.bufferedImage.height}") }
            .let { it.resize((it.bufferedImage.width.toDouble() / pixelsPerSecond.toDouble() * samplesPerSecond.toDouble()).roundToInt(), 256 / 2 + 1) }
            .fromLogScale()
            .generatePhase()
            .generatePhaseGriffinLim(5, { it.toWaveform(8, 1.0) }, { it.toSpectrogram(8, 1.0) })
            .toWaveform(8, 1 / 1600.0)
            .toWavByteArray()
            .also { it.writeTo(File("$baseName.wav")) }
            .toOggAsWav()
            .writeTo(File("$baseName.ogg"))
    }
}

private val assetsDir = File(System.getProperty("user.home")).resolve(".gradle/caches/fabric-loom/assets")
private val outputDir = File("./build/minecraft_assets")

private fun File.mkdirsParentOrThrow() {
    val parentDir = this.absoluteFile.parentFile
    if (!parentDir.isDirectory && !parentDir.mkdirs()) throw RuntimeException("failed to create directory: $parentDir")
}

private class MinecraftAsset(val name: String, val hash: String) {
    companion object {
        fun getMinecraftAssets(): List<MinecraftAsset> {
            val indexes = assetsDir.resolve("indexes/1.20.1-5.json").readText().toJsonElement().toJsonWrapper()
            return indexes["objects"].asMap().map { obj ->
                MinecraftAsset(obj.key, obj.value["hash"].asString())
            }
        }
    }
}

object ExtractMinecraftAssetsMain {
    @JvmStatic
    fun main(args: Array<String>) {
        MinecraftAsset.getMinecraftAssets().forEach { asset ->
            logger.info("${asset.name} ${asset.hash}")
            val inputFile = assetsDir.resolve("objects/${asset.hash.take(2)}/${asset.hash}")
            val outputFile = outputDir.resolve("original/${asset.name.replace("/", "__")}")
            if (!outputFile.exists()) {
                outputFile.mkdirsParentOrThrow()
                inputFile.copyTo(outputFile)
            }
        }
    }
}

object DegenerateMain {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val onProgressChanged = mutableListOf<(count: Int, total: Int) -> Unit>()
        val onStarted = mutableListOf<() -> Unit>()
        val onFinished = mutableListOf<() -> Unit>()

        JFrame().also { f ->
            f.add(JButton().also { b ->
                b.preferredSize = Dimension(200, 50)
                b.addActionListener {
                    this@runBlocking.cancel()
                    f.dispose()
                }
                onProgressChanged += { count, total ->
                    b.text = "Interrupt ($count/$total)"
                }
            })
            f.isLocationByPlatform = true
            f.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
            onFinished += {
                f.dispose()
            }
            onStarted += {
                f.isVisible = true
            }
            f.pack()
        }

        val assets = MinecraftAsset.getMinecraftAssets()
            .filter { it.name.endsWith(".ogg") }
        var count = 0

        onProgressChanged.forEach { it(count, assets.size) }
        onStarted.forEach { it() }
        try {
            coroutineScope {
                assets.forEach { asset ->
                    launch(Dispatchers.Default) {
                        val baseName = asset.name.dropLast(4).replace("/", "__")

                        val inputFile = outputDir.resolve("original/$baseName.ogg")
                        val outputDegenerateFile = outputDir.resolve("degenerate/$baseName.png")
                        if (!outputDegenerateFile.exists()) {
                            logger.info("Start degenerate: ${asset.name}")
                            try {
                                degenerate(inputFile, outputDegenerateFile)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        }

                        val outputCopyFile = outputDir.resolve("degenerate/$baseName.ogg")
                        if (!outputCopyFile.exists()) {
                            inputFile.copyTo(outputCopyFile)
                        }

                        this@runBlocking.launch {
                            count++
                            onProgressChanged.forEach { it(count, assets.size) }
                        }
                    }
                }
            }
        } finally {
            onFinished.forEach { it() }
        }

    }

    private fun degenerate(inputFile: File, outputFile: File) {
        val waveform = inputFile
            .readBytes()
            .toWavAsOgg()
            .toWaveformAsWav()
        if (waveform.doubleArray.size > samplesPerSecond * 10) throw RuntimeException("too long: ${inputFile.name} (${waveform.doubleArray.size.toDouble() / samplesPerSecond.toDouble()}s)")
        logger.info("Waveform Length: ${waveform.doubleArray.size} samples") // 56256 == 56511 - 255

        val spectrogram = waveform.toSpectrogram(8, 1 / 800.0)
        logger.info("Image Size: ${spectrogram.bufferedImage.width} x ${spectrogram.bufferedImage.height}") // 56511 x 129
        // 画像の幅のうち、255は固定の部分に使われる
        // 画像の幅から-255した部分の長さが実際のサンプル数に相当する
        // 画像の幅が+128される度にサンプル数が+48000になってほしい

        spectrogram
            .removePhase()
            .resize((spectrogram.bufferedImage.width.toDouble() / samplesPerSecond.toDouble() * pixelsPerSecond.toDouble()).roundToInt(), 128) // 151 x 128
            .toLogScale()
            .writeTo(outputFile.also { it.mkdirsParentOrThrow() })
    }
}

object RegenerateMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val baseName = "minecraft__sounds__block__cauldron__dye1"
        val inputFile = outputDir.resolve("degenerate/$baseName.png")
        val outputFile = outputDir.resolve("regenerate/$baseName.ogg")
        regenerate(inputFile, outputFile)
    }

    private fun regenerate(inputFile: File, outputFile: File) {
        val spectrogram = inputFile
            .readSpectrogram()
        logger.info("Image Size: ${spectrogram.bufferedImage.width} x ${spectrogram.bufferedImage.height}") // 151 x 128

        spectrogram
            .resize((spectrogram.bufferedImage.width.toDouble() / pixelsPerSecond.toDouble() * samplesPerSecond.toDouble()).roundToInt(), 256 / 2 + 1) // 56625 x 129
            .fromLogScale()
            .generatePhase()
            .generatePhaseGriffinLim(5, { it.toWaveform(8, 1.0) }, { it.toSpectrogram(8, 1.0) })
            .toWaveform(8, 1 / 800.0)
            .toWavByteArray()
            .toOggAsWav()
            .writeTo(outputFile.also { it.mkdirsParentOrThrow() })
    }
}
