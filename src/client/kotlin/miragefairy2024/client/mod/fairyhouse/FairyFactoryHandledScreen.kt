package miragefairy2024.client.mod.fairyhouse

import miragefairy2024.client.util.drawRightText
import miragefairy2024.mod.fairyhouse.AbstractFairyHouseCard
import miragefairy2024.mod.fairyhouse.FairyFactoryScreenHandler
import miragefairy2024.util.text
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

abstract class FairyFactoryHandledScreen<H : FairyFactoryScreenHandler>(card: AbstractFairyHouseCard<*, *>, arguments: Arguments<H>) : HandledScreen<H>(arguments.handler, arguments.playerInventory, arguments.title) {

    class Arguments<H : FairyFactoryScreenHandler>(val handler: H, val playerInventory: PlayerInventory, val title: Text)

    init {
        backgroundWidth = card.guiWidth
        backgroundHeight = card.guiHeight
        playerInventoryTitleY = backgroundHeight - 94
    }

    override fun init() {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    abstract fun getBackgroundTexture(): Identifier

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        context.drawTexture(getBackgroundTexture(), x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        super.drawForeground(context, mouseX, mouseY)
        context.drawRightText(textRenderer, text { "${handler.folia}"() }, backgroundWidth - 5, titleY, 0x404040, false)
    }

}
