package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.setFairyMotif
import miragefairy2024.mod.rei.FairyFamilyReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.toEntryStack
import net.minecraft.text.Text

object FairyFamilyClientReiCategoryCard : ClientReiCategoryCard<FairyFamilyReiCategoryCard.Display>(FairyFamilyReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        // TODO
    }

    override fun createCategory() = object : DisplayCategory<FairyFamilyReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = FairyFamilyReiCategoryCard.identifier
        override fun getTitle(): Text = FairyFamilyReiCategoryCard.translation()
        override fun getIcon(): Renderer = FairyCard.item.createItemStack().also { it.setFairyMotif(MotifCard.IRON) }.toEntryStack()
        override fun getDisplayWidth(display: FairyFamilyReiCategoryCard.Display) = 18 * 9 + 10
        override fun getDisplayHeight() = 18 * 2 + 10
        override fun setupDisplay(display: FairyFamilyReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            return listOf(
                Widgets.createRecipeBase(bounds),
                // TODO
            )
        }
    }
}
