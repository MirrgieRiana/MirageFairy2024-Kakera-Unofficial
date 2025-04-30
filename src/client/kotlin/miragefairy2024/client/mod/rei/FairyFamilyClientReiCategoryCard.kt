package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.mod.rei.FairyFamilyReiCategoryCard
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import net.minecraft.network.chat.Component

object FairyFamilyClientReiCategoryCard : ClientReiCategoryCard<FairyFamilyReiCategoryCard.Display>(FairyFamilyReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        val childrenTable = motifRegistry.entrySet()
            .flatMap { it.value.parents.map { parent -> parent to it.value } }
            .groupBy { it.first }
            .mapValues { it.value.map { pair -> pair.second } }

        //fun Motif.getAncestors(): List<Motif> = this.parents.flatMap { it.getAncestors() } + this.parents
        //fun Motif.getDescendants(): List<Motif> = childrenTable.getOrElse(this) { listOf() }.flatMap { it.getDescendants() } + childrenTable.getOrElse(this) { listOf() }

        motifRegistry.sortedEntrySet.forEach {
            val parents = it.value.parents
            val children = childrenTable[it.value] ?: listOf()
            if (parents.isNotEmpty() || children.isNotEmpty()) {
                registry.add(FairyFamilyReiCategoryCard.Display(it.value, parents, children))
            }
        }
    }

    override fun createCategory() = object : DisplayCategory<FairyFamilyReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = FairyFamilyReiCategoryCard.identifier.first
        override fun getTitle(): Component = text { FairyFamilyReiCategoryCard.translation() }
        override fun getIcon(): Renderer = MotifCard.IRON.createFairyItemStack().toEntryStack()
        override fun getDisplayWidth(display: FairyFamilyReiCategoryCard.Display) = 18 * 9 + 10
        override fun getDisplayHeight() = 18 * 7 + 10
        override fun setupDisplay(display: FairyFamilyReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val widgets = mutableListOf<Widget>()

            widgets += Widgets.createRecipeBase(bounds)

            // 上1列に親妖精
            run {
                val p = bounds.location + Point(5, 5)
                widgets += Widgets.createSlotBase(Rectangle(p.x, p.y, 18 * 9, 18 * 1))
                display.parents.forEachIndexed { index, motif ->
                    val r = index / 9
                    val c = index % 9
                    val itemStack = motif.createFairyItemStack()
                    widgets += Widgets.createSlot(p + Point(1 + 18 * c, 1 + 18 * r)).entries(listOf(itemStack.toEntryStack()).toEntryIngredient()).disableBackground().markOutput()
                }
            }

            // 中段にメイン妖精
            run {
                val p = bounds.location + Point(5 + 18 * 4, 5 + 18 * 1 + 9)
                val itemStack = display.motif.createFairyItemStack()
                widgets += Widgets.createSlot(p + Point(1, 1)).entries(listOf(itemStack.toEntryStack()).toEntryIngredient()).markInput()
            }

            // 下4列に子妖精
            run {
                val p = bounds.location + Point(5, 5 + 18 * 3)
                widgets += Widgets.createSlotBase(Rectangle(p.x, p.y, 18 * 9, 18 * 4))
                display.children.forEachIndexed { index, motif ->
                    val r = index / 9
                    val c = index % 9
                    val itemStack = motif.createFairyItemStack()
                    widgets += Widgets.createSlot(p + Point(1 + 18 * c, 1 + 18 * r)).entries(listOf(itemStack.toEntryStack()).toEntryIngredient()).disableBackground().markOutput()
                }
            }

            return widgets
        }
    }
}
