package miragefairy2024.client.util

import net.minecraft.network.chat.Component
import com.mojang.blaze3d.vertex.PoseStack as MatrixStack
import net.minecraft.client.gui.Font as TextRenderer
import net.minecraft.client.gui.GuiGraphics as DrawContext
import net.minecraft.util.FormattedCharSequence as OrderedText

inline fun <T> MatrixStack.stack(block: () -> T): T {
    this.pushPose()
    try {
        return block()
    } finally {
        this.popPose()
    }
}

fun DrawContext.drawRightText(textRenderer: TextRenderer, text: String, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawString(textRenderer, text, rightX - textRenderer.width(text), y, color, shadow)
}

fun DrawContext.drawRightText(textRenderer: TextRenderer, text: Component, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    val orderedText = text.visualOrderText
    this.drawString(textRenderer, orderedText, rightX - textRenderer.width(orderedText), y, color, shadow)
}

fun DrawContext.drawRightText(textRenderer: TextRenderer, text: OrderedText, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawString(textRenderer, text, rightX - textRenderer.width(text), y, color, shadow)
}
