package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.harvestNotations
import miragefairy2024.mod.rei.HarvestReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryStack
import net.minecraft.network.chat.Component

object HarvestClientReiCategoryCard : ClientReiCategoryCard<HarvestReiCategoryCard.Display>(HarvestReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        harvestNotations.forEach { recipe ->
            registry.add(HarvestReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<HarvestReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = HarvestReiCategoryCard.identifier.first
        override fun getTitle(): Component = text { HarvestReiCategoryCard.translation() }
        override fun getIcon(): Renderer = MaterialCard.VEROPEDA_BERRIES.item.createItemStack().toEntryStack()
        override fun getDisplayWidth(display: HarvestReiCategoryCard.Display) = 136
        override fun getDisplayHeight() = 36
        override fun setupDisplay(display: HarvestReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(3, 3)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createSlotBackground(p + Point(15 - 8, 15 - 8)), // 入力スロット背景
                Widgets.createSlot(p + Point(15 - 8, 15 - 8)).entries(display.inputEntries[0]).disableBackground().markInput(), // 入力アイテム

                Widgets.createSlotBase(Rectangle(p.x + 28 + 15 - 8 - 5, p.y + 15 - 8 - 5, 16 * 5 + 2 * 4 + 10, 16 + 10)), // 出力スロット背景
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 0, 15 - 8)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 1, 15 - 8)).entries(display.outputEntries.getOrNull(1) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 2, 15 - 8)).entries(display.outputEntries.getOrNull(2) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 3, 15 - 8)).entries(display.outputEntries.getOrNull(3) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(28 + 15 - 8 + (16 + 2) * 4, 15 - 8)).entries(display.outputEntries.getOrNull(4) ?: EntryIngredient.empty()).disableBackground().markOutput(), // 出力アイテム
            )
        }
    }
}
