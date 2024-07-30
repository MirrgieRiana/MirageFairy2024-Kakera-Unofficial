package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.util.drawRightText
import miragefairy2024.mod.fairyhouse.FairyFactoryScreenHandler
import miragefairy2024.mod.fairyhouse.FairyHouseCard
import miragefairy2024.mod.fairyhouse.FairyHouseModelCard
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun initFairyHouseClientModule() {
    ModelLoadingPlugin.register {
        FairyHouseModelCard.entries.forEach { card ->
            it.addModels(card.identifier)
        }
    }

    HandledScreens.register(FairyHouseCard.screenHandlerType) { gui, inventory, title -> FairyHouseScreen(gui, inventory, title) }
}


class FairyHouseScreen(
    handler: FairyFactoryScreenHandler,
    playerInventory: PlayerInventory,
    title: Text,
) : HandledScreen<FairyFactoryScreenHandler>(
    handler,
    playerInventory,
    title,
) {
    companion object {
        private val TEXTURE = Identifier(MirageFairy2024.modId, "textures/gui/container/fairy_house.png")
    }

    private val card = FairyHouseCard

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
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        super.drawForeground(context, mouseX, mouseY)
        context.drawRightText(textRenderer, text { "${handler.folia}"() }, backgroundWidth - 5, titleY, 0x404040, false)
    }

}
