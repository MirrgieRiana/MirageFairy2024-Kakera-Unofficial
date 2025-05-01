package miragefairy2024.colormaker

import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel

class Layer(val image: BufferedImage, val colorExpression: ColorExpression)

class LayeredImage(private val zoom: Int) : JLabel() {
    var colorEvaluator = ColorEvaluator()
    var backgroundColor: Color = Color.black

    fun render(arrayList: List<Layer>) {
        icon = ImageIcon(createImage(arrayList))
    }

    private fun createImage(layers: List<Layer>): BufferedImage {
        val image = BufferedImage(16 * zoom, 16 * zoom, BufferedImage.TYPE_INT_RGB)
        repeat(16 * zoom) { x ->
            repeat(16 * zoom) { y ->

                // ラベルの背景色で初期化
                var r1 = backgroundColor.red
                var g1 = backgroundColor.green
                var b1 = backgroundColor.blue

                layers.forEach { layer ->

                    // 乗算する色
                    val colorMul = colorEvaluator.evaluate(layer.colorExpression)

                    // 画像の色
                    val argbOver = layer.image.getRGB(x / zoom, y / zoom)
                    val a2 = (argbOver shr 24) and 0xff
                    var r2 = (argbOver shr 16) and 0xff
                    var g2 = (argbOver shr 8) and 0xff
                    var b2 = (argbOver shr 0) and 0xff

                    // 画像の色を乗算する色で更新
                    r2 = r2 * colorMul.red / 255
                    g2 = g2 * colorMul.green / 255
                    b2 = b2 * colorMul.blue / 255

                    // 現在の色を更新
                    r1 = (r1 * (255 - a2) + r2 * a2) / 255
                    g1 = (g1 * (255 - a2) + g2 * a2) / 255
                    b1 = (b1 * (255 - a2) + b2 * a2) / 255

                }

                // 色セット
                image.setRGB(x, y, ((r1 and 0xff) shl 16) or ((g1 and 0xff) shl 8) or ((b1 and 0xff) shl 0))

            }
        }
        return image
    }
}
