package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.fairybuilding.FOLIA_TRANSLATION
import miragefairy2024.mod.fairybuilding.FairyBuildingCard
import miragefairy2024.mod.fairybuilding.FairyBuildingModelCard
import miragefairy2024.mod.fairybuilding.FairyBuildingScreenHandler
import miragefairy2024.mod.fairybuilding.FairyCollectorCard
import miragefairy2024.mod.fairybuilding.FairyCollectorScreenHandler
import miragefairy2024.mod.fairybuilding.FairyFactoryCard
import miragefairy2024.mod.fairybuilding.FairyFactoryScreenHandler
import miragefairy2024.mod.fairybuilding.FairyHouseCard
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atMost
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import java.util.Optional
import kotlin.math.roundToInt

fun initFairyBuildingClientModule() {
    ModelLoadingPlugin.register {
        FairyBuildingModelCard.entries.forEach { card ->
            it.addModels(card.identifier)
        }
    }

    HandledScreens.register(FairyHouseCard.screenHandlerType) { gui, inventory, title -> FairyHouseScreen(FairyBuildingScreen.Arguments(gui, inventory, title)) }
    HandledScreens.register(FairyCollectorCard.screenHandlerType) { gui, inventory, title -> FairyCollectorScreen(FairyBuildingScreen.Arguments(gui, inventory, title)) }
}

open class FairyBuildingScreen<H : FairyBuildingScreenHandler>(private val card: FairyBuildingCard<*, *, H>, arguments: Arguments<H>) :
    HandledScreen<H>(arguments.handler, arguments.playerInventory, arguments.title) {
    companion object {
        val SPRITES_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/fairy_building.png")
    }

    class Arguments<H : FairyBuildingScreenHandler>(val handler: H, val playerInventory: PlayerInventory, val title: Text)

    init {
        backgroundWidth = card.configuration.guiWidth
        backgroundHeight = card.configuration.guiHeight
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
            if (slot.hasStack()) return@run
            val slotConfiguration = card.slots.getOrNull(slot.index) ?: return@run
            val toolTipGetter = slotConfiguration.toolTipGetter ?: return@run
            context.drawTooltip(textRenderer, toolTipGetter(), Optional.empty(), x, y)
        }
    }

}

open class FairyFactoryScreen<H : FairyFactoryScreenHandler>(private val card: FairyFactoryCard<*, *, H>, arguments: Arguments<H>) : FairyBuildingScreen<H>(card, arguments) {
    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        super.drawBackground(context, delta, mouseX, mouseY)
        val h = (9.0 * (handler.folia / card.configuration.maxFolia.toDouble() atMost 1.0)).roundToInt()
        context.drawTexture(SPRITES_TEXTURE, x + 164, y + backgroundHeight - 94 + (9 - h), 32F, 0F + (9 - h).toFloat(), 5, h, 64, 64)
    }

    override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
        super.drawMouseoverTooltip(context, x, y)
        run {
            if (x in this.x + 164 until this.x + 164 + 5 && y in this.y + backgroundHeight - 94 until this.y + backgroundHeight - 94 + 9) {
                val texts = listOf(
                    text { FOLIA_TRANSLATION() },
                    text { "${handler.folia} / ${card.configuration.maxFolia}"() },
                )
                context.drawTooltip(textRenderer, texts, Optional.empty(), x, y)
            }
        }
    }
}

class FairyHouseScreen(arguments: Arguments<FairyFactoryScreenHandler>) : FairyFactoryScreen<FairyFactoryScreenHandler>(FairyHouseCard, arguments)

class FairyCollectorScreen(arguments: Arguments<FairyCollectorScreenHandler>) : FairyFactoryScreen<FairyCollectorScreenHandler>(FairyCollectorCard, arguments) {
    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        super.drawBackground(context, delta, mouseX, mouseY)
        context.drawTexture(SPRITES_TEXTURE, x + 14, y + 28, 0F, 0F, (18.0 * (handler.collectionProgress / 10000.0 atMost 1.0)).roundToInt(), 4, 64, 64)
        context.drawTexture(SPRITES_TEXTURE, x + 80, y + 28, 0F, 0F, (18.0 * (handler.sortProgress / 10000.0 atMost 1.0)).roundToInt(), 4, 64, 64)
    }

    override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
        super.drawMouseoverTooltip(context, x, y)
        run {
            if (x in this.x + 19 until this.x + 19 + 18 && y in this.y + 28 until this.y + 28 + 4) {
                context.drawTooltip(textRenderer, listOf(text { "${handler.collectionProgress} / 10000 (+${handler.collectionSpeed * 20}/s)"() }), Optional.empty(), x, y)
            }
            if (x in this.x + 76 until this.x + 76 + 18 && y in this.y + 28 until this.y + 28 + 4) {
                context.drawTooltip(textRenderer, listOf(text { "${handler.sortProgress} / 10000 (+${handler.sortSpeed * 20}/s)"() }), Optional.empty(), x, y)
            }
        }
    }
}
