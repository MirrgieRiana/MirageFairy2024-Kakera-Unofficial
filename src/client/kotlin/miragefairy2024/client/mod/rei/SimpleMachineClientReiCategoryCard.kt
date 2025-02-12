package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.FermentationBarrelScreen
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelRecipe
import miragefairy2024.mod.machine.FermentationBarrelRecipeCard
import miragefairy2024.mod.rei.FermentationBarrelReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.minus
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.formatAs
import mirrg.kotlin.hydrogen.stripTrailingZeros

object FermentationBarrelClientReiCategoryCard : ClientReiCategoryCard<FermentationBarrelReiCategoryCard.Display>(FermentationBarrelReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        registry.registerRecipeFiller(FermentationBarrelRecipe::class.java, FermentationBarrelRecipeCard.type) {
            FermentationBarrelReiCategoryCard.Display(it)
        }
    }

    override fun createCategory() = object : DisplayCategory<FermentationBarrelReiCategoryCard.Display> {
        val imageX = 30 - 8
        val imageY = 16 - 4
        val imageWidth = 120 + 8 * 2
        val imageHeight = 40 + 4 * 2
        override fun getCategoryIdentifier() = FermentationBarrelReiCategoryCard.identifier.first
        override fun getTitle() = text { FermentationBarrelReiCategoryCard.translation() }
        override fun getIcon(): Renderer = FermentationBarrelCard.item.createItemStack().toEntryStack()
        override fun getDisplayWidth(display: FermentationBarrelReiCategoryCard.Display) = imageWidth + 6
        override fun getDisplayHeight() = imageHeight + 6
        override fun setupDisplay(display: FermentationBarrelReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(3, 3)
            val uv = Point(imageX, imageY)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createTexturedWidget(MirageFairy2024.identifier("textures/gui/container/fermentation_barrel.png"), Rectangle(bounds.x + 3, bounds.y + 3, bounds.width - 6, bounds.height - 6), uv.x.toFloat(), uv.y.toFloat()),

                Widgets.createSlot(p + Point(42, 17) - uv).entries(display.inputEntries[0]).disableBackground().markInput(),
                Widgets.createSlot(p + Point(31, 39) - uv).entries(display.inputEntries[1]).disableBackground().markInput(),
                Widgets.createSlot(p + Point(53, 39) - uv).entries(display.inputEntries[2]).disableBackground().markInput(),

                Widgets.createArrow(p + Point(77 - 1, 27) - uv).animationDurationTicks(display.recipe.duration.toDouble()),
                Widgets.createLabel(p + Point(77 - 1 + (22 + 2) / 2, 27 - 12) - uv, text { translate("category.rei.campfire.time", (display.recipe.duration.toDouble() / 20.0 formatAs "%.2f").stripTrailingZeros()) }).centered().color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow(),

                Widgets.createSlot(p + Point(111, 28) - uv).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).disableBackground().markOutput(),
            )
        }
    }

    override fun getWorkstations() = listOf(listOf(FermentationBarrelCard.item.createItemStack().toEntryStack()).toEntryIngredient())

    override fun registerScreens(registry: ScreenRegistry) {
        registry.registerContainerClickArea(Rectangle(77 - 1, 27, 22 + 2, 16 + 2), FermentationBarrelScreen::class.java, FermentationBarrelReiCategoryCard.identifier.first)
    }
}
