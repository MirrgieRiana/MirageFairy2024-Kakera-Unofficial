package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.mod.fermentationbarrel.FermentationBarrelCard
import miragefairy2024.mod.fermentationbarrel.FermentationBarrelScreenHandler
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreens
import java.util.Optional
import kotlin.math.roundToInt

fun initFermentationBarrelClientModule() {
    HandledScreens.register(FermentationBarrelCard.screenHandlerType) { gui, inventory, title -> FermentationBarrelScreen(FermentationBarrelCard, MachineScreen.Arguments(gui, inventory, title)) }
}

class FermentationBarrelScreen(card: FermentationBarrelCard, arguments: Arguments<FermentationBarrelScreenHandler>) : MachineScreen<FermentationBarrelScreenHandler>(card, arguments) {
    companion object {
        val PROGRESS_ARROW_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/progress.png")
        private const val ARROW_X = 77
        private const val ARROW_Y = 27
        private const val ARROW_WIDTH = 22
        private const val ARROW_HEIGHT = 16
    }

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        super.drawBackground(context, delta, mouseX, mouseY)

        if (handler.progressMax > 0) {
            val w = (ARROW_WIDTH.toDouble() * (handler.progress.toDouble() / handler.progressMax.toDouble() atMost 1.0)).roundToInt()
            context.drawTexture(PROGRESS_ARROW_TEXTURE, x + ARROW_X, y + ARROW_Y, 0F, 0F, w, ARROW_HEIGHT, 32, 32)
        }
    }

    override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
        super.drawMouseoverTooltip(context, x, y)
        run {
            if (x in this.x + ARROW_X until this.x + ARROW_X + ARROW_WIDTH && y in this.y + ARROW_Y until this.y + ARROW_Y + ARROW_HEIGHT) {
                context.drawTooltip(textRenderer, listOf(text { "${handler.progress} / ${handler.progressMax}"() }), Optional.empty(), x, y + 17)
            }
        }
    }
}
