package miragefairy2024.client.util

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import miragefairy2024.MirageFairy2024
import net.minecraft.text.Text
import net.minecraft.util.Identifier

enum class SlotType(val texture: Identifier) {
    NORMAL(Identifier(MirageFairy2024.modId, "textures/gui/slot.png")),
    FAIRY(Identifier(MirageFairy2024.modId, "textures/gui/fairy_slot.png")),
}

fun slotContainer(slotComponent: Component, type: SlotType = SlotType.NORMAL): FlowLayout {
    return Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
        padding(Insets.of(1))
        surface(Surface.tiled(type.texture, 18, 18))
        allowOverflow(true)
        child(slotComponent)
    }
}

fun horizontalSpace(width: Int): FlowLayout = Containers.verticalFlow(Sizing.fixed(width), Sizing.content())

fun verticalSpace(height: Int): FlowLayout = Containers.verticalFlow(Sizing.content(), Sizing.fixed(height))

fun inventoryNameLabel(name: Text, horizontalTextAlignment: HorizontalAlignment? = HorizontalAlignment.LEFT): LabelComponent = Components.label(name).apply {
    margins(Insets.of(0, 0, 1, 1))
    sizing(Sizing.fill(100), Sizing.content())
    horizontalTextAlignment(horizontalTextAlignment)
    color(Color.ofRgb(0x404040))
}

fun verticalScroll(horizontalSizing: Sizing, verticalSizing: Sizing, scrollbarThiccness: Int): ScrollContainer<FlowLayout> {
    return Containers.verticalScroll(horizontalSizing, verticalSizing, Containers.verticalFlow(horizontalSizing, Sizing.content()).apply {
        padding(Insets.of(0, 0, 0, scrollbarThiccness))
    }).apply {
        scrollbarThiccness(scrollbarThiccness)
    }
}

fun horizontalScroll(horizontalSizing: Sizing, verticalSizing: Sizing, scrollbarThiccness: Int): ScrollContainer<FlowLayout> {
    return Containers.horizontalScroll(horizontalSizing, verticalSizing, Containers.horizontalFlow(Sizing.content(), verticalSizing).apply {
        padding(Insets.of(0, scrollbarThiccness, 0, 0))
    }).apply {
        scrollbarThiccness(scrollbarThiccness)
    }
}
