package miragefairy2024.client.util

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence

inline fun <T> PoseStack.stack(block: () -> T): T {
    this.pushPose()
    try {
        return block()
    } finally {
        this.popPose()
    }
}

fun GuiGraphics.drawRightText(textRenderer: Font, text: String, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawString(textRenderer, text, rightX - textRenderer.width(text), y, color, shadow)
}

fun GuiGraphics.drawRightText(textRenderer: Font, text: Component, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    val orderedText = text.visualOrderText
    this.drawString(textRenderer, orderedText, rightX - textRenderer.width(orderedText), y, color, shadow)
}

fun GuiGraphics.drawRightText(textRenderer: Font, text: FormattedCharSequence, rightX: Int, y: Int, color: Int, shadow: Boolean) {
    this.drawString(textRenderer, text, rightX - textRenderer.width(text), y, color, shadow)
}
