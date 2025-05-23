package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.client.lib.MachineScreen
import miragefairy2024.client.util.registerHandledScreen
import miragefairy2024.mod.machine.AuraReflectorFurnaceCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceScreenHandler
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelScreenHandler
import miragefairy2024.mod.machine.SimpleMachineCard
import miragefairy2024.mod.machine.SimpleMachineScreenHandler
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.client.renderer.Rect2i
import java.util.Optional
import kotlin.math.roundToInt
import net.minecraft.client.gui.GuiGraphics as DrawContext

context(ModContext)
fun initMachineClientModule() {
    FermentationBarrelCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> FermentationBarrelScreen(FermentationBarrelCard, MachineScreen.Arguments(gui, inventory, title)) }
    AuraReflectorFurnaceCard.screenHandlerType.registerHandledScreen { gui, inventory, title -> AuraReflectorFurnaceScreen(AuraReflectorFurnaceCard, MachineScreen.Arguments(gui, inventory, title)) }
}

abstract class SimpleMachineScreen<H : SimpleMachineScreenHandler>(card: SimpleMachineCard<*, *, *, *>, arguments: Arguments<H>) : MachineScreen<H>(card, arguments) {
    companion object {
        val PROGRESS_ARROW_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/progress.png")
    }

    abstract val arrowBound: Rect2i

    override fun renderBg(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)

        if (menu.progressMax > 0) {
            val w = (arrowBound.width.toDouble() * (menu.progress.toDouble() / menu.progressMax.toDouble() atMost 1.0)).roundToInt()
            context.blit(
                PROGRESS_ARROW_TEXTURE,
                leftPos + arrowBound.x,
                topPos + arrowBound.y - 1,
                0F,
                0F,
                w,
                arrowBound.height + 1,
                32,
                32,
            )
        }
    }

    override fun renderTooltip(context: DrawContext, x: Int, y: Int) {
        super.renderTooltip(context, x, y)
        run {
            val bound = Rect2i(
                this.leftPos + arrowBound.x,
                this.topPos + arrowBound.y - 1,
                arrowBound.width - 1,
                arrowBound.height + 1 - 1,
            )
            if (bound.contains(x, y)) {
                context.renderTooltip(font, listOf(text { "${menu.progress} / ${menu.progressMax}"() }), Optional.empty(), x, y + 17)
            }
        }
    }
}

class FermentationBarrelScreen(card: FermentationBarrelCard, arguments: Arguments<FermentationBarrelScreenHandler>) : SimpleMachineScreen<FermentationBarrelScreenHandler>(card, arguments) {
    override val arrowBound = Rect2i(77, 28, 22, 15)
}

class AuraReflectorFurnaceScreen(card: AuraReflectorFurnaceCard, arguments: Arguments<AuraReflectorFurnaceScreenHandler>) : SimpleMachineScreen<AuraReflectorFurnaceScreenHandler>(card, arguments) {
    companion object {
        val BLUE_FUEL_TEXTURE = MirageFairy2024.identifier("textures/gui/sprites/blue_fuel.png")
    }

    override val arrowBound = Rect2i(89, 35, 22, 15)
    val fuelBound = Rect2i(48, 37, 13, 13)

    override fun renderBg(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        super.renderBg(context, delta, mouseX, mouseY)

        if (menu.fuelMax > 0) {
            val h = (fuelBound.height.toDouble() * (menu.fuel.toDouble() / menu.fuelMax.toDouble() atMost 1.0)).roundToInt()
            context.blit(
                BLUE_FUEL_TEXTURE,
                leftPos + fuelBound.x - 1,
                topPos + fuelBound.y - 1 + (fuelBound.height - h),
                0F,
                fuelBound.height.toFloat() - h.toFloat(),
                fuelBound.width,
                h,
                32,
                32,
            )
        }
    }
}
