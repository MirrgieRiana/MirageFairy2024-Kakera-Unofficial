package miragefairy2024.client.mod

import miragefairy2024.client.util.drawRightText
import miragefairy2024.mod.fairybuilding.FairyBuildingCard
import miragefairy2024.mod.fairybuilding.FairyBuildingModelCard
import miragefairy2024.mod.fairybuilding.FairyBuildingScreenHandler
import miragefairy2024.mod.fairybuilding.FairyCollectorCard
import miragefairy2024.mod.fairybuilding.FairyCollectorScreenHandler
import miragefairy2024.mod.fairybuilding.FairyFactoryScreenHandler
import miragefairy2024.mod.fairybuilding.FairyHouseCard
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

fun initFairyBuildingClientModule() {
    ModelLoadingPlugin.register {
        FairyBuildingModelCard.entries.forEach { card ->
            it.addModels(card.identifier)
        }
    }

    HandledScreens.register(FairyHouseCard.screenHandlerType) { gui, inventory, title -> FairyHouseScreen(FairyBuildingScreen.Arguments(gui, inventory, title)) }
    HandledScreens.register(FairyCollectorCard.screenHandlerType) { gui, inventory, title -> FairyCollectorScreen(FairyBuildingScreen.Arguments(gui, inventory, title)) }
}

open class FairyBuildingScreen<H : FairyBuildingScreenHandler>(
    private val card: FairyBuildingCard<*, H>,
    arguments: Arguments<H>
) : HandledScreen<H>(arguments.handler, arguments.playerInventory, arguments.title) {

    class Arguments<H : FairyBuildingScreenHandler>(val handler: H, val playerInventory: PlayerInventory, val title: Text)

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
        context.drawTexture(card.backgroundTexture, x, y, 0, 0, backgroundWidth, backgroundHeight)
    }

}

open class FairyFactoryScreen<H : FairyFactoryScreenHandler>(card: FairyBuildingCard<*, H>, arguments: Arguments<H>) : FairyBuildingScreen<H>(card, arguments) {

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        super.drawForeground(context, mouseX, mouseY)
        context.drawRightText(textRenderer, text { "${handler.folia}"() }, backgroundWidth - 5, titleY, 0x404040, false)
    }

}

class FairyHouseScreen(arguments: Arguments<FairyFactoryScreenHandler>) : FairyFactoryScreen<FairyFactoryScreenHandler>(FairyHouseCard, arguments)

class FairyCollectorScreen(arguments: Arguments<FairyCollectorScreenHandler>) : FairyFactoryScreen<FairyCollectorScreenHandler>(FairyCollectorCard, arguments)
