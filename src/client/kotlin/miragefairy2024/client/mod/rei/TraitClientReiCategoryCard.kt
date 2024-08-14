package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.rei.TraitReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.toEntryStack
import net.minecraft.text.Text

object TraitClientReiCategoryCard : ClientReiCategoryCard<TraitReiCategoryCard.Display>(TraitReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {

    }

    override fun createCategory() = object : DisplayCategory<TraitReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = TraitReiCategoryCard.identifier.first
        override fun getTitle(): Text = TraitReiCategoryCard.translation()
        override fun getIcon(): Renderer = MirageFlowerCard.item.createItemStack().toEntryStack()
        override fun getDisplayWidth(display: TraitReiCategoryCard.Display) = 180
        override fun getDisplayHeight() = 36
        override fun setupDisplay(display: TraitReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(3, 3)
            return listOf(
                Widgets.createRecipeBase(bounds),

                Widgets.createResultSlotBackground(p + Point(15 - 8, 15 - 8)), // 出力スロット背景
                Widgets.createSlot(p + Point(15 - 8, 15 - 8)).entries(display.outputEntries[0]).disableBackground().markOutput(), // 出力アイテム
            )
        }
    }
}
