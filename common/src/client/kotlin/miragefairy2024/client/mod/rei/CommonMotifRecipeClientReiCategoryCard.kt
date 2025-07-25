package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import miragefairy2024.mod.fairy.AlwaysCommonMotifRecipe
import miragefairy2024.mod.fairy.BiomeCommonMotifRecipe
import miragefairy2024.mod.fairy.BiomeTagCommonMotifRecipe
import miragefairy2024.mod.fairy.COMMON_MOTIF_RECIPES
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.materials.item.MIRAGE_FLOUR_TAG
import miragefairy2024.mod.rei.COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION
import miragefairy2024.mod.rei.CommonMotifRecipeReiCategoryCard
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIngredient
import miragefairy2024.util.translate
import net.minecraft.network.chat.Component

object CommonMotifRecipeClientReiCategoryCard : ClientReiCategoryCard<CommonMotifRecipeReiCategoryCard.Display>(CommonMotifRecipeReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        COMMON_MOTIF_RECIPES.sortedBy {
            when (it) {
                is AlwaysCommonMotifRecipe -> "always:"
                is BiomeCommonMotifRecipe -> "biome:" + it.biome.location().string
                is BiomeTagCommonMotifRecipe -> "biome_tag:" + it.biomeTag.location().string
            }
        }.forEach { recipe ->
            registry.add(CommonMotifRecipeReiCategoryCard.Display(recipe))
        }
    }

    override fun createCategory() = object : DisplayCategory<CommonMotifRecipeReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = CommonMotifRecipeReiCategoryCard.identifier.first
        override fun getTitle(): Component = text { CommonMotifRecipeReiCategoryCard.translation() }
        override fun getIcon(): Renderer = MotifCard.WATER.createFairyItemStack().toEntryStack()
        override fun getDisplayWidth(display: CommonMotifRecipeReiCategoryCard.Display) = 160
        override fun getDisplayHeight() = 3 + 2 + 18 + 2 + 3
        override fun setupDisplay(display: CommonMotifRecipeReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            val recipeText = when (val recipe = display.recipe) {
                is AlwaysCommonMotifRecipe -> text { COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION() }
                is BiomeCommonMotifRecipe -> text { translate(recipe.biome.location().toLanguageKey("biome")) }
                is BiomeTagCommonMotifRecipe -> text { recipe.biomeTag.location().path() }
            }
            return listOf(
                Widgets.createRecipeBase(bounds),
                Widgets.createLabel(p + Point(0, 5), recipeText)
                    .color(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
                    .let {
                        when (val recipe = display.recipe) {
                            is AlwaysCommonMotifRecipe -> it
                            is BiomeCommonMotifRecipe -> it
                            is BiomeTagCommonMotifRecipe -> it.tooltip(text { recipe.biomeTag.location().string() })
                        }
                    }
                    .noShadow()
                    .leftAligned(),
                Widgets.createSlot(p + Point(133, 1)).entries(display.outputEntries[0]).markOutput(), // 出力アイテム
            )
        }
    }

    override fun getWorkstations() = listOf(MIRAGE_FLOUR_TAG.toIngredient().items.map { it.toEntryStack() }.toEntryIngredient())
}
