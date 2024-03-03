package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.fairy.COMMON_MOTIF_RECIPES
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.setFairyMotif
import miragefairy2024.mod.rei.CommonMotifRecipeReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toEntryStack
import net.minecraft.text.Text

object CommonMotifRecipeClientReiCategoryCard : ClientReiCategoryCard<CommonMotifRecipeReiCategoryCard.Display>(CommonMotifRecipeReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        COMMON_MOTIF_RECIPES.sortedBy { it.biome?.id?.string }.forEach { recipe ->
            registry.add(CommonMotifRecipeReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<CommonMotifRecipeReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = CommonMotifRecipeReiCategoryCard.identifier
        override fun getTitle(): Text = CommonMotifRecipeReiCategoryCard.translation()
        override fun getIcon(): Renderer = FairyCard.item.createItemStack().also { it.setFairyMotif(MotifCard.WATER) }.toEntryStack()
        override fun getDisplayWidth(display: CommonMotifRecipeReiCategoryCard.Display) = 160
        override fun getDisplayHeight() = 3 + 2 + 18 + 2 + 3
        override fun setupDisplay(display: CommonMotifRecipeReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            return listOf(
                Widgets.createRecipeBase(bounds),
                Widgets.createLabel(p + Point(0, 5), text { display.recipe.biome?.let { it.id.path() } ?: "Always"() }) // TODO translate
                    .color(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
                    .let {
                        val biome = display.recipe.biome
                        if (biome != null) it.tooltip(text { biome.id.string() }) else it
                    }
                    .noShadow()
                    .leftAligned(),
                Widgets.createSlot(p + Point(133, 1)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).markOutput(), // 出力アイテム
            )
        }
    }
}
