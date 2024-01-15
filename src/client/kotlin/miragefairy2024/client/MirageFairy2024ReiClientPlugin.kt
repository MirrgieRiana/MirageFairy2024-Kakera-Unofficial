package miragefairy2024.client

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.FairyQuestRecipeReiCategoryCard
import miragefairy2024.MagicPlantCropReiCategoryCard
import miragefairy2024.ReiCategoryCard
import miragefairy2024.WorldGenTraitReiCategoryCard
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.fairyquest.FairyQuestRecipeCard
import miragefairy2024.mod.fairyquest.fairyQuestRecipeRegistry
import miragefairy2024.mod.fairyquest.setFairyQuestRecipe
import miragefairy2024.mod.magicplant.WorldGenTraitRecipe
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.magicPlantCropNotations
import miragefairy2024.mod.magicplant.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.worldGenTraitRecipeRegistry
import miragefairy2024.util.createItemStack
import miragefairy2024.util.formatted
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryStack
import net.minecraft.text.Text

abstract class ClientReiCategoryCard<D : BasicDisplay>(val parent: ReiCategoryCard<D>) {
    companion object {
        val entries = listOf(
            WorldGenTraitClientReiCategoryCard,
            MagicPlantCropClientReiCategoryCard,
            FairyQuestRecipeClientReiCategoryCard,
        )
    }

    abstract fun registerDisplays(registry: DisplayRegistry)
    abstract fun createCategory(): DisplayCategory<D>
}

@Suppress("unused")
class MirageFairy2024ReiClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            registry.add(card.createCategory())
        }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerDisplays(registry)
        }
    }
}

object WorldGenTraitClientReiCategoryCard : ClientReiCategoryCard<WorldGenTraitReiCategoryCard.Display>(WorldGenTraitReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        worldGenTraitRecipeRegistry.values.flatten().forEach { recipe ->
            registry.add(WorldGenTraitReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<WorldGenTraitReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = WorldGenTraitReiCategoryCard.identifier
        override fun getTitle(): Text = WorldGenTraitReiCategoryCard.translation()
        override fun getIcon(): Renderer = MirageFlowerCard.item.createItemStack().toEntryStack()
        override fun getDisplayWidth(display: WorldGenTraitReiCategoryCard.Display) = 180
        override fun getDisplayHeight() = 36
        override fun setupDisplay(display: WorldGenTraitReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val rarityText = when (display.recipe.rarity) {
                WorldGenTraitRecipe.Rarity.A -> text { "100%"() }
                WorldGenTraitRecipe.Rarity.C -> text { ">99%"() }
                WorldGenTraitRecipe.Rarity.N -> text { "<90%"() }
                WorldGenTraitRecipe.Rarity.R -> text { "<8%"() }
                WorldGenTraitRecipe.Rarity.S -> text { "<1%"() }
            }
            val traitStackText = text { (display.recipe.trait.getName() + " "() + display.recipe.level.toString(2)()).formatted(display.recipe.trait.color) }
            val p = bounds.location + Point(3, 3)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createResultSlotBackground(p + Point(15 - 8, 15 - 8)), // 出力スロット背景
                Widgets.createSlot(p + Point(15 - 8, 15 - 8)).entries(display.outputEntries[0]).disableBackground().markOutput(), // 出力アイテム

                Widgets.createLabel(p + Point(50, 5), rarityText).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow(), // 出現頻度
                Widgets.createLabel(p + Point(70, 5), display.recipe.condition.description).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow().leftAligned(), // 条件
                Widgets.createLabel(p + Point(30, 17), traitStackText).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow().leftAligned(), // 特性
            )
        }
    }
}

object MagicPlantCropClientReiCategoryCard : ClientReiCategoryCard<MagicPlantCropReiCategoryCard.Display>(MagicPlantCropReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        magicPlantCropNotations.forEach { recipe ->
            registry.add(MagicPlantCropReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<MagicPlantCropReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = MagicPlantCropReiCategoryCard.identifier
        override fun getTitle(): Text = MagicPlantCropReiCategoryCard.translation()
        override fun getIcon(): Renderer = MaterialCard.VEROPEDA_BERRIES.item.createItemStack().toEntryStack()
        override fun getDisplayWidth(display: MagicPlantCropReiCategoryCard.Display) = 136
        override fun getDisplayHeight() = 36
        override fun setupDisplay(display: MagicPlantCropReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
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

object FairyQuestRecipeClientReiCategoryCard : ClientReiCategoryCard<FairyQuestRecipeReiCategoryCard.Display>(FairyQuestRecipeReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        fairyQuestRecipeRegistry.forEach { recipe ->
            registry.add(FairyQuestRecipeReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<FairyQuestRecipeReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = FairyQuestRecipeReiCategoryCard.identifier
        override fun getTitle(): Text = FairyQuestRecipeReiCategoryCard.translation()
        override fun getIcon(): Renderer = FairyQuestCardCard.item.createItemStack().also { it.setFairyQuestRecipe(FairyQuestRecipeCard.NEW_PRODUCT_FROM_FRI) }.toEntryStack()
        override fun getDisplayWidth(display: FairyQuestRecipeReiCategoryCard.Display) = 18 * 9 + 10
        override fun getDisplayHeight() = 18 * 2 + 10
        override fun setupDisplay(display: FairyQuestRecipeReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createSlot(p + Point(18 * 0, 18 * 0)).entries(display.inputEntries.getOrNull(0) ?: EntryIngredient.empty()).disableBackground().markInput(), // フェアリークエストカード
                Widgets.createLabel(p + Point(18 * 1 + 2, 18 * 0 + 9 - 5), display.recipe.title).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow().leftAligned(),

                Widgets.createSlot(p + Point(18 * 0, 18 * 1)).entries(display.inputEntries.getOrNull(1) ?: EntryIngredient.empty()).markInput(), // 入力アイテム
                Widgets.createSlot(p + Point(18 * 1, 18 * 1)).entries(display.inputEntries.getOrNull(2) ?: EntryIngredient.empty()).markInput(), // 入力アイテム
                Widgets.createSlot(p + Point(18 * 2, 18 * 1)).entries(display.inputEntries.getOrNull(3) ?: EntryIngredient.empty()).markInput(), // 入力アイテム
                Widgets.createSlot(p + Point(18 * 3, 18 * 1)).entries(display.inputEntries.getOrNull(4) ?: EntryIngredient.empty()).markInput(), // 入力アイテム

                Widgets.createSlot(p + Point(18 * 5, 18 * 1)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(18 * 6, 18 * 1)).entries(display.outputEntries.getOrNull(1) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(18 * 7, 18 * 1)).entries(display.outputEntries.getOrNull(2) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
                Widgets.createSlot(p + Point(18 * 8, 18 * 1)).entries(display.outputEntries.getOrNull(3) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
            )
        }
    }
}
