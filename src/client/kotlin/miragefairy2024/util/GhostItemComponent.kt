package miragefairy2024.util

import com.mojang.blaze3d.systems.RenderSystem
import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.tooltip.TooltipComponent
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.item.ItemStack
import kotlin.jvm.optionals.getOrNull

class GhostItemComponent(var itemStack: ItemStack = EMPTY_ITEM_STACK) : BaseComponent() {

    var showItemStack = true
    var overlayColor: Int = 0x00000000
    var showOverlay = false

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        val notSideLit = !MinecraftClient.getInstance().itemRenderer.getModel(itemStack, null, null, 0).isSideLit
        if (notSideLit) DiffuseLighting.disableGuiDepthLighting()
        if (showItemStack) context.drawItem(itemStack, x, y) // アイテム本体
        if (showItemStack) context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, itemStack, x, y) // 個数
        RenderSystem.disableDepthTest()
        if (showItemStack) context.drawGradientRect(x, y, width, height, 0x808B8B8B.toInt(), 0x808B8B8B.toInt(), 0x808B8B8B.toInt(), 0x808B8B8B.toInt()) // アイテムを半透明にするやつ
        if (showOverlay) context.drawGradientRect(x, y, width, height, overlayColor, overlayColor, overlayColor, overlayColor) // オーバーレイ
        RenderSystem.enableDepthTest()
        if (notSideLit) DiffuseLighting.enableGuiDepthLighting()
    }

    override fun tooltip(): MutableList<TooltipComponent> {
        val tooltip = mutableListOf<TooltipComponent>()

        val context = if (MinecraftClient.getInstance().options.advancedItemTooltips) TooltipContext.ADVANCED else TooltipContext.BASIC
        val texts = itemStack.getTooltip(MinecraftClient.getInstance().player, context)
        tooltip += texts.map { TooltipComponent.of(it.asOrderedText()) }

        val data = itemStack.tooltipData.getOrNull()
        if (data != null) tooltip.add(1, TooltipComponentCallback.EVENT.invoker().getComponent(data) ?: TooltipComponent.of(data))

        return tooltip
    }

    override fun determineHorizontalContentSize(sizing: Sizing) = 16
    override fun determineVerticalContentSize(sizing: Sizing) = 16
}
