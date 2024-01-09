package miragefairy2024.util

import io.wispforest.owo.ui.container.WrappingParentComponent
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing

class ClickableContainer<C : Component>(horizontalSizing: Sizing, verticalSizing: Sizing, private val action: () -> Boolean, child: () -> C) : WrappingParentComponent<C>(horizontalSizing, verticalSizing, child()) {
    override fun canFocus(source: Component.FocusSource) = source == Component.FocusSource.KEYBOARD_CYCLE

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        super.draw(context, mouseX, mouseY, partialTicks, delta)
        drawChildren(context, mouseX, mouseY, partialTicks, delta, childView)
    }

    override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (super.onMouseDown(mouseX, mouseY, button)) return true
        return action()
    }
}
