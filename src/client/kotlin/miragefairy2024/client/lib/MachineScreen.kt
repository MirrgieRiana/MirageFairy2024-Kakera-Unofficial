package miragefairy2024.client.lib

import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import java.util.Optional

open class MachineScreen<H : MachineScreenHandler>(private val card: MachineCard<*, *, *>, arguments: Arguments<H>) : HandledScreen<H>(arguments.handler, arguments.playerInventory, arguments.title) {

    class Arguments<H>(val handler: H, val playerInventory: PlayerInventory, val title: Text)

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

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.drawTexture(card.backgroundTexture, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
        super.drawMouseoverTooltip(context, x, y)
        run {
            val slot = focusedSlot ?: return@run
            val tooltip = handler.getTooltip(slot) ?: return@run
            context.drawTooltip(textRenderer, tooltip, Optional.empty(), x, y)
        }
    }

}
