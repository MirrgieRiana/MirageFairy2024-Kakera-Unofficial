package miragefairy2024.client.lib

import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import net.minecraft.client.gui.GuiGraphics as DrawContext
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen as HandledScreen
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.network.chat.Component as Text
import java.util.Optional

open class MachineScreen<H : MachineScreenHandler>(private val card: MachineCard<*, *, *>, arguments: Arguments<H>) : HandledScreen<H>(arguments.handler, arguments.playerInventory, arguments.title) {

    class Arguments<H>(val handler: H, val playerInventory: PlayerInventory, val title: Text)

    init {
        imageWidth = card.guiWidth
        imageHeight = card.guiHeight
        inventoryLabelY = imageHeight - 94
    }

    override fun init() {
        super.init()
        titleLabelX = (imageWidth - font.width(title)) / 2
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        renderTooltip(context, mouseX, mouseY)
    }

    override fun renderBg(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        context.blit(card.backgroundTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    }

    override fun renderTooltip(context: DrawContext, x: Int, y: Int) {
        super.renderTooltip(context, x, y)
        run {
            val slot = hoveredSlot ?: return@run
            val tooltip = menu.getTooltip(slot) ?: return@run
            context.renderTooltip(font, tooltip, Optional.empty(), x, y)
        }
    }

}
