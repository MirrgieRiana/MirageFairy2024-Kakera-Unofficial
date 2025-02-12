package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelScreenHandler
import miragefairy2024.mod.machine.SimpleMachineCard
import miragefairy2024.mod.machine.SimpleMachineScreenHandler
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.util.math.Rect2i
import java.util.Optional
import kotlin.math.roundToInt

fun initMachineClientModule() {
    HandledScreens.register(FermentationBarrelCard.screenHandlerType) { gui, inventory, title -> FermentationBarrelScreen(FermentationBarrelCard, MachineScreen.Arguments(gui, inventory, title)) }
}

abstract class SimpleMachineScreen<H : SimpleMachineScreenHandler>(card: SimpleMachineCard<*, *, *, *>, arguments: Arguments<H>) : MachineScreen<H>(card, arguments) {
    companion object {
        val PROGRESS_ARROW_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/progress.png")
    }

    abstract val arrowBound: Rect2i

    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        super.drawBackground(context, delta, mouseX, mouseY)

        if (handler.progressMax > 0) {
            val w = (arrowBound.width.toDouble() * (handler.progress.toDouble() / handler.progressMax.toDouble() atMost 1.0)).roundToInt()
            context.drawTexture(PROGRESS_ARROW_TEXTURE, x + arrowBound.x, y + arrowBound.y, 0F, 0F, w, arrowBound.height, 32, 32)
        }
    }

    override fun drawMouseoverTooltip(context: DrawContext, x: Int, y: Int) {
        super.drawMouseoverTooltip(context, x, y)
        run {
            if (x in this.x + arrowBound.x until this.x + arrowBound.x + arrowBound.width && y in this.y + arrowBound.y until this.y + arrowBound.y + arrowBound.height) {
                context.drawTooltip(textRenderer, listOf(text { "${handler.progress} / ${handler.progressMax}"() }), Optional.empty(), x, y + 17)
            }
        }
    }
}

class FermentationBarrelScreen(card: FermentationBarrelCard, arguments: Arguments<FermentationBarrelScreenHandler>) : SimpleMachineScreen<FermentationBarrelScreenHandler>(card, arguments) {
    override val arrowBound = Rect2i(77, 27, 22, 16)
}
