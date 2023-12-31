package miragefairy2024.util

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.container.WrappingParentComponent
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.surface
import net.minecraft.text.Text

fun slotContainer(slotComponent: Component): FlowLayout = Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
    padding(Insets.of(1))
    surface(NinePatchTextureCard.SLOT.surface)
    child(slotComponent)
}

fun horizontalSpace(width: Int): FlowLayout = Containers.verticalFlow(Sizing.fixed(width), Sizing.content())

fun verticalSpace(height: Int): FlowLayout = Containers.verticalFlow(Sizing.content(), Sizing.fixed(height))

fun inventoryNameLabel(name: Text, horizontalTextAlignment: HorizontalAlignment? = HorizontalAlignment.LEFT): LabelComponent = Components.label(name).apply {
    margins(Insets.of(0, 0, 1, 1))
    sizing(Sizing.fill(), Sizing.content())
    horizontalTextAlignment(horizontalTextAlignment)
    color(Color.ofRgb(0x404040))
}

fun <C : Component> verticalScroll(horizontalSizing: Sizing, verticalSizing: Sizing, configurator: ScrollContainer<C>.() -> Unit, creator: () -> C): ScrollContainer<C> {
    return Containers.verticalScroll(horizontalSizing, verticalSizing, creator()).also { configurator(it) }
}

fun <C : Component> horizontalScroll(horizontalSizing: Sizing, verticalSizing: Sizing, configurator: ScrollContainer<C>.() -> Unit, creator: () -> C): ScrollContainer<C> {
    return Containers.horizontalScroll(horizontalSizing, verticalSizing, creator()).also { configurator(it) }
}

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
