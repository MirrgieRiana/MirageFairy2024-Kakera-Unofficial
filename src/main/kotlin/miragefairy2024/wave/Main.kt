package miragefairy2024.wave

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirrg.kotlin.gson.hydrogen.toJsonElement
import mirrg.kotlin.gson.hydrogen.toJsonWrapper
import mirrg.kotlin.hydrogen.join
import mirrg.kotlin.hydrogen.max
import mirrg.kotlin.slf4j.hydrogen.getFileLogger
import java.awt.Color
import java.awt.Dimension
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JButton
import javax.swing.JFrame

private val logger = getFileLogger(object {})

object GenerateV1Main {
    @JvmStatic
    fun main(args: Array<String>) {

        GenerateV1.generate(File("""./src/main/resources/assets/miragefairy2024/sounds/magic_hit.8b.1000ms.8000a.scr.png"""))

        //File("""./src/main/resources/assets/miragefairy2024/sounds""").listFiles()?.forEach { inputFile ->
        //    if (inputFile.name.endsWith(".scr.png")) convert(inputFile)
        //}

    }
}


object TmpMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val dir = File("./src/main/resources/assets/miragefairy2024/sounds")
        val image1 = ImageIO.read(dir.resolve("無題.png"))
        val image2 = ImageIO.read(dir.resolve("無題2.png"))
        val conversions: List<Pair<String, (x: Int, y: Int, r: Int, g: Int, b: Int) -> Any?>> = listOf(
            "r" to { x, y, r, g, b -> r },
            "g" to { x, y, r, g, b -> g },
            "b" to { x, y, r, g, b -> b },
            "h" to { x, y, r, g, b -> Color.RGBtoHSB(r, g, b, null)[0] },
            "s" to { x, y, r, g, b -> Color.RGBtoHSB(r, g, b, null)[1] },
            "v" to { x, y, r, g, b -> Color.RGBtoHSB(r, g, b, null)[2] },
            "La" to { x, y, r, g, b -> (r + g + b) / (255 * 3).toDouble() },
            "Lm" to { x, y, r, g, b -> r max g max b },
            "L2a" to { x, y, r, g, b -> (r * 76.0 + g * 149.0 + b * 29.0) / (255 * (76 + 149 + 29)).toDouble() },
        )
        println(
            listOf(
                "x",
                "y",
                *conversions.map { (name, _) -> "${name}1" }.toTypedArray(),
                *conversions.map { (name, _) -> "${name}2" }.toTypedArray(),
            ).join(",")
        )
        (0 until 16).forEach { x ->
            (0 until 16).forEach { y ->
                val rgb1 = image1.getRGB(x, y)
                val rgb2 = image2.getRGB(x, y)
                val r1 = rgb1 shr 16 and 0xFF
                val g1 = rgb1 shr 8 and 0xFF
                val b1 = rgb1 and 0xFF
                val r2 = rgb2 shr 16 and 0xFF
                val g2 = rgb2 shr 8 and 0xFF
                val b2 = rgb2 and 0xFF
                println(
                    listOf(
                        x.toString(),
                        y.toString(),
                        *conversions.map { (_, f) -> f(x, y, r1, g1, b1).toString() }.toTypedArray(),
                        *conversions.map { (_, f) -> f(x, y, r2, g2, b2).toString() }.toTypedArray(),
                    ).join(",")
                )
            }
        }
    }
}

object GenerateV2Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val tasks = mutableListOf<() -> Unit>()

        val dir = File("./src/main/resources/assets/miragefairy2024/sounds")
        fun generate(baseFileName: String) {
            tasks += {
                GenerateV2.generate(dir.resolve("$baseFileName.sc2.png"), dir.resolve("$baseFileName.ogg"), dumpWav = true)
            }
        }

        //generate("entity_chaos_cube_ambient_1")
        //generate("entity_chaos_cube_ambient_2")
        //generate("entity_chaos_cube_hurt_1")
        //generate("entity_chaos_cube_death_1")
        //generate("001")
        //generate("002")
        //generate("004")
        //generate("005")
        //generate("006")
        //generate("007")
        //generate("008")
        //generate("009")
        //generate("010")
        //generate("011")
        //generate("012")
        generate("013")

        //tasks += {
        //    val baseFileName = "009"
        //    GenerateV2.generate(dir.resolve("$baseFileName.scr.png"), dir.resolve("$baseFileName.ogg"), dumpWav = true)
        //    GenerateV2.degenerate(dir.resolve("$baseFileName.ogg"), dir.resolve("$baseFileName.2.png"))
        //}

        runBlocking {
            tasks.forEach { task ->
                launch(Dispatchers.Default) {
                    task()
                }
            }
        }
    }
}

private val assetsDir = File(System.getProperty("user.home")).resolve(".gradle/caches/fabric-loom/assets")
private val outputDir = File("./build/minecraft_assets")

fun File.mkdirsParentOrThrow() {
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
                                GenerateV2.degenerate(inputFile, outputDegenerateFile)
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
}

object RegenerateMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val baseName = "minecraft__sounds__block__cauldron__dye1"
        GenerateV2.generate(outputDir.resolve("degenerate/$baseName.png"), outputDir.resolve("regenerate/$baseName.ogg"))
    }
}
