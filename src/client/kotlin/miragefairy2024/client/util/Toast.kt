package miragefairy2024.client.util

import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Size
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.toast.Toast
import net.minecraft.client.toast.ToastManager

fun createOwoToast(component: Component) = object : Toast {

    init {
        component.inflate(Size.of(1000, 1000))
        component.mount(null, 0, 0)
    }

    private var startTime: Long = 0
    private var justUpdated = false

    override fun draw(context: DrawContext, manager: ToastManager, startTime: Long): Toast.Visibility {
        if (this.justUpdated) {
            this.startTime = startTime
            this.justUpdated = false
        }

        component.draw(OwoUIDrawContext.of(context), -1000, -1000, MinecraftClient.getInstance().tickDelta, MinecraftClient.getInstance().lastFrameDuration)

        return if ((startTime - this.startTime).toDouble() >= 5000.0 * manager.notificationDisplayTimeMultiplier) Toast.Visibility.HIDE else Toast.Visibility.SHOW
    }

    override fun getWidth(): Int = component.fullSize().width()
    override fun getHeight(): Int = component.fullSize().height()

}
