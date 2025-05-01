package miragefairy2024.client.util

import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Size
import net.minecraft.client.gui.components.toasts.Toast
import io.wispforest.owo.ui.core.Component as OwoComponent
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.gui.GuiGraphics as DrawContext
import net.minecraft.client.gui.components.toasts.ToastComponent as ToastManager

fun createOwoToast(component: OwoComponent) = object : Toast {

    init {
        component.inflate(Size.of(1000, 1000))
        component.mount(null, 0, 0)
    }

    private var startTime: Long = 0
    private var justUpdated = false

    override fun render(context: DrawContext, manager: ToastManager, startTime: Long): Toast.Visibility {
        if (this.justUpdated) {
            this.startTime = startTime
            this.justUpdated = false
        }

        component.draw(OwoUIDrawContext.of(context), -1000, -1000, MinecraftClient.getInstance().timer.getGameTimeDeltaPartialTick(true), MinecraftClient.getInstance().timer.realtimeDeltaTicks)

        return if ((startTime - this.startTime).toDouble() >= 5000.0 * manager.notificationDisplayTimeMultiplier) Toast.Visibility.HIDE else Toast.Visibility.SHOW
    }

    override fun width(): Int = component.fullSize().width()
    override fun height(): Int = component.fullSize().height()

}
