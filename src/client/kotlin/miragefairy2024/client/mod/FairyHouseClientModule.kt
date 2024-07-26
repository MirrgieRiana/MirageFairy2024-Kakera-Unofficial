package miragefairy2024.client.mod

import miragefairy2024.mod.fairyhouse.AbstractFairyHouseScreenHandler
import miragefairy2024.mod.fairyhouse.FairyHouseBlockEntity
import miragefairy2024.mod.fairyhouse.FairyHouseCard
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun initFairyHouseClientModule() {
    HandledScreens.register(FairyHouseCard.screenHandlerType) { gui, inventory, title -> FairyHouseScreen(gui, inventory, title) }
}


class FairyHouseScreen(
    handler: AbstractFairyHouseScreenHandler<FairyHouseBlockEntity>,
    playerInventory: PlayerInventory,
    title: Text,
) : HandledScreen<AbstractFairyHouseScreenHandler<FairyHouseBlockEntity>>(
    handler,
    playerInventory,
    title,
) {
    companion object {
        private val TEXTURE = Identifier("textures/gui/container/brewing_stand.png")
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

}
