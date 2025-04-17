package miragefairy2024.client.util

import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import miragefairy2024.util.EMPTY_ITEM_STACK
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent as TooltipComponent
import net.minecraft.world.item.TooltipFlag as TooltipContext
import com.mojang.blaze3d.platform.Lighting as DiffuseLighting
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

class GhostItemComponent(var itemStacks: List<ItemStack> = listOf()) : BaseComponent() {
    var showItemStack = true
    var overlayColor: Int = 0x00000000
    var showOverlay = false

    var time = 0.0

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        if (!Screen.hasControlDown()) time += delta

        val itemStack = if (itemStacks.isNotEmpty()) itemStacks[(time / 30.0).toInt() % itemStacks.size] else EMPTY_ITEM_STACK

        val notSideLit = !MinecraftClient.getInstance().itemRenderer.getModel(itemStack, null, null, 0).isSideLit


        // アイテム本体
        if (showItemStack) {
            if (notSideLit) DiffuseLighting.setupForFlatItems()
            context.renderItem(itemStack, x, y)
            if (notSideLit) DiffuseLighting.setupFor3DItems()
        }

        // 個数
        if (showItemStack) context.renderItemDecorations(MinecraftClient.getInstance().textRenderer, itemStack, x, y)

        context.matrices.stack {
            context.matrices.translate(0F, 0F, 200F)
            if (showItemStack) context.drawGradientRect(x, y, width, height, 0x808B8B8B.toInt(), 0x808B8B8B.toInt(), 0x808B8B8B.toInt(), 0x808B8B8B.toInt()) // アイテムを半透明にするやつ
            if (showOverlay) context.drawGradientRect(x, y, width, height, overlayColor, overlayColor, overlayColor, overlayColor) // オーバーレイ
        }

    }

    /** スロットのツールチップに通常のTooltip機能を使用すると、スロットのオーバーレイの四角形が後にレンダリングされ表示が壊れます。 */
    fun drawGhostTooltip(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        if (!shouldDrawTooltip(mouseX.toDouble(), mouseY.toDouble())) return

        val itemStack = if (itemStacks.isNotEmpty()) itemStacks[(time / 30.0).toInt() % itemStacks.size] else EMPTY_ITEM_STACK
        if (itemStack.isEmpty) return

        val tooltip = mutableListOf<TooltipComponent>()

        val tooltipContext = if (MinecraftClient.getInstance().options.advancedItemTooltips) TooltipContext.ADVANCED else TooltipContext.BASIC
        val texts = itemStack.getTooltipLines(MinecraftClient.getInstance().player, tooltipContext)
        tooltip += texts.map { TooltipComponent.create(it.getVisualOrderText()) }

        val data = itemStack.tooltipData.getOrNull()
        if (data != null) tooltip.add(1, TooltipComponentCallback.EVENT.invoker().getComponent(data) ?: TooltipComponent.create(data))

        context.drawTooltip(MinecraftClient.getInstance().textRenderer, mouseX, mouseY, tooltip)
        context.flush()
    }

    override fun determineHorizontalContentSize(sizing: Sizing) = 16
    override fun determineVerticalContentSize(sizing: Sizing) = 16
}
