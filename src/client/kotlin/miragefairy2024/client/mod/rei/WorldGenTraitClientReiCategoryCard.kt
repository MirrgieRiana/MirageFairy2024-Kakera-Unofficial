package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import miragefairy2024.mod.magicplant.TraitSpawnRarity
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.worldGenTraitRecipeRegistry
import miragefairy2024.mod.rei.WorldGenTraitReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.formatted
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryStack
import net.minecraft.text.Text

object WorldGenTraitClientReiCategoryCard : ClientReiCategoryCard<WorldGenTraitReiCategoryCard.Display>(WorldGenTraitReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        worldGenTraitRecipeRegistry.values.flatten().forEach { recipe ->
            registry.add(WorldGenTraitReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<WorldGenTraitReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = WorldGenTraitReiCategoryCard.identifier.first
        override fun getTitle(): Text = WorldGenTraitReiCategoryCard.translation()
        override fun getIcon(): Renderer = MirageFlowerCard.item.createItemStack().toEntryStack()
        override fun getDisplayWidth(display: WorldGenTraitReiCategoryCard.Display) = 180
        override fun getDisplayHeight() = 36
        override fun setupDisplay(display: WorldGenTraitReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val rarityText = when (display.recipe.rarity) {
                TraitSpawnRarity.ALWAYS -> text { "100%"() }
                TraitSpawnRarity.COMMON -> text { ">99%"() }
                TraitSpawnRarity.NORMAL -> text { "<90%"() }
                TraitSpawnRarity.RARE -> text { "<8%"() }
                TraitSpawnRarity.S_RARE -> text { "<1%"() }
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
