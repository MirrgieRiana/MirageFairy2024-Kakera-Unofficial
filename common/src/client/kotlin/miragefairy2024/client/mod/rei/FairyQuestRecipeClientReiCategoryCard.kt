package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.fairyquest.FairyQuestRecipeCard
import miragefairy2024.mod.fairyquest.fairyQuestRecipeRegistry
import miragefairy2024.mod.fairyquest.setFairyQuestRecipe
import miragefairy2024.mod.rei.FairyQuestRecipeReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryStack
import net.minecraft.network.chat.Component

object FairyQuestRecipeClientReiCategoryCard : ClientReiCategoryCard<FairyQuestRecipeReiCategoryCard.Display>(FairyQuestRecipeReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        fairyQuestRecipeRegistry.forEach { recipe ->
            registry.add(FairyQuestRecipeReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<FairyQuestRecipeReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = FairyQuestRecipeReiCategoryCard.identifier.first
        override fun getTitle(): Component = text { FairyQuestRecipeReiCategoryCard.translation() }
        override fun getIcon(): Renderer = FairyQuestCardCard.item().createItemStack().also { it.setFairyQuestRecipe(FairyQuestRecipeCard.NEW_PRODUCT_FROM_FRI) }.toEntryStack()
        override fun getDisplayWidth(display: FairyQuestRecipeReiCategoryCard.Display) = 18 * 9 + 10
        override fun getDisplayHeight() = 18 * 2 + 10
        override fun setupDisplay(display: FairyQuestRecipeReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createSlot(p + Point(18 * 0, 18 * 0)).entries(display.inputEntries.getOrNull(0) ?: EntryIngredient.empty()).disableBackground().markInput(), // フェアリークエストカード
                Widgets.createLabel(p + Point(18 * 1 + 2, 18 * 0 + 9 - 5), display.recipe.title).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow().leftAligned(),

                Widgets.createSlot(p + Point(1 + 18 * 0, 1 + 18 * 1)).entries(display.inputEntries.getOrNull(1) ?: EntryIngredient.empty()).markInput(), // 入力アイテム
                Widgets.createSlot(p + Point(1 + 18 * 1, 1 + 18 * 1)).entries(display.inputEntries.getOrNull(2) ?: EntryIngredient.empty()).markInput(), // 入力アイテム
                Widgets.createSlot(p + Point(1 + 18 * 2, 1 + 18 * 1)).entries(display.inputEntries.getOrNull(3) ?: EntryIngredient.empty()).markInput(), // 入力アイテム
                Widgets.createSlot(p + Point(1 + 18 * 3, 1 + 18 * 1)).entries(display.inputEntries.getOrNull(4) ?: EntryIngredient.empty()).markInput(), // 入力アイテム

                Widgets.createSlot(p + Point(1 + 18 * 5, 1 + 18 * 1)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(1 + 18 * 6, 1 + 18 * 1)).entries(display.outputEntries.getOrNull(1) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(1 + 18 * 7, 1 + 18 * 1)).entries(display.outputEntries.getOrNull(2) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(1 + 18 * 8, 1 + 18 * 1)).entries(display.outputEntries.getOrNull(3) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
            )
        }
    }
}
