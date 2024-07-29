package miragefairy2024.client.util

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.OrderedText
import net.minecraft.text.Text

inline fun <T> MatrixStack.stack(block: () -> T): T {
    this.push()
    try {
        return block()
    } finally {
        this.pop()
    }
}

fun DrawContext.drawRightText(textRenderer: TextRenderer, text: String, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawText(textRenderer, text, rightX - textRenderer.getWidth(text), y, color, shadow)
}

fun DrawContext.drawRightText(textRenderer: TextRenderer, text: Text, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    val orderedText = text.asOrderedText()
    this.drawText(textRenderer, orderedText, rightX - textRenderer.getWidth(orderedText), y, color, shadow)
}

fun DrawContext.drawRightText(textRenderer: TextRenderer, text: OrderedText, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawText(textRenderer, text, rightX - textRenderer.getWidth(text), y, color, shadow)
}
