package miragefairy2024.client.mod.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.MIRAGE_FLOUR_TAG
import miragefairy2024.mod.fairy.FairyDreamRecipes
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.fairyDreamContainer
import miragefairy2024.mod.rei.BlockFairyDreamRecipeReiCategoryCard
import miragefairy2024.mod.rei.EntityTypeFairyDreamRecipeReiCategoryCard
import miragefairy2024.mod.rei.ItemFairyDreamRecipeReiCategoryCard
import miragefairy2024.mod.rei.ReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.darkRed
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIngredient
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.text.Text

abstract class BaseFairyDreamRecipeClientReiCategoryCard<T, D : BasicDisplay>(parent: ReiCategoryCard<D>) : ClientReiCategoryCard<D>(parent)

object ItemFairyDreamRecipeClientReiCategoryCard : BaseFairyDreamRecipeClientReiCategoryCard<Item, ItemFairyDreamRecipeReiCategoryCard.Display>(ItemFairyDreamRecipeReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        FairyDreamRecipes.ITEM.getDisplayMap().forEach { (motif, keys) ->
            registry.add(ItemFairyDreamRecipeReiCategoryCard.Display(keys.toList(), motif))
        }
    }

    override fun createCategory() = object : DisplayCategory<ItemFairyDreamRecipeReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = ItemFairyDreamRecipeReiCategoryCard.identifier.first
        override fun getTitle(): Text = ItemFairyDreamRecipeReiCategoryCard.translation()
        override fun getIcon(): Renderer = MotifCard.CARROT.createFairyItemStack().toEntryStack()
        override fun getDisplayWidth(display: ItemFairyDreamRecipeReiCategoryCard.Display) = 160
        override fun getDisplayHeight() = 28
        override fun setupDisplay(display: ItemFairyDreamRecipeReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            val gained = MinecraftClient.getInstance().player!!.fairyDreamContainer[display.motif]
            val text = text { display.items[0].name }
                .let { if (display.items.size > 1) text { it + "..."() } else it }
                .let { if (!gained) it.darkRed else it }
            return listOf(
                Widgets.createRecipeBase(bounds),
                Widgets.createSlot(p + Point(1, 1)).entries(display.inputEntries[0]).disableBackground(),
                Widgets.createLabel(p + Point(21, 5), text)
                    .leftAligned()
                    .color(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
                    .noShadow()
                    .tooltip(display.items.map { it.name }.join(text { "\n"() })),
                Widgets.createSlot(p + Point(133, 1)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).markOutput(),
            )
        }
    }

    override fun getWorkstations() = listOf(MIRAGE_FLOUR_TAG.toIngredient().matchingStacks.map { it.toEntryStack() }.toEntryIngredient())
}

object BlockFairyDreamRecipeClientReiCategoryCard : BaseFairyDreamRecipeClientReiCategoryCard<Block, BlockFairyDreamRecipeReiCategoryCard.Display>(BlockFairyDreamRecipeReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        FairyDreamRecipes.BLOCK.getDisplayMap().forEach { (motif, keys) ->
            registry.add(BlockFairyDreamRecipeReiCategoryCard.Display(keys.toList(), motif))
        }
    }

    override fun createCategory() = object : DisplayCategory<BlockFairyDreamRecipeReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = BlockFairyDreamRecipeReiCategoryCard.identifier.first
        override fun getTitle(): Text = BlockFairyDreamRecipeReiCategoryCard.translation()
        override fun getIcon(): Renderer = MotifCard.MAGENTA_GLAZED_TERRACOTTA.createFairyItemStack().toEntryStack()
        override fun getDisplayWidth(display: BlockFairyDreamRecipeReiCategoryCard.Display) = 160
        override fun getDisplayHeight() = 28
        override fun setupDisplay(display: BlockFairyDreamRecipeReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            val gained = MinecraftClient.getInstance().player!!.fairyDreamContainer[display.motif]
            val text = text { display.blocks[0].name }
                .let { if (display.blocks.size > 1) text { it + "..."() } else it }
                .let { if (!gained) it.darkRed else it }
            return listOf(
                Widgets.createRecipeBase(bounds),
                Widgets.createSlot(p + Point(1, 1)).entries(display.blocks[0].asItem().createItemStack().toEntryStack().toEntryIngredient()).disableBackground(),
                Widgets.createLabel(p + Point(21, 5), text)
                    .leftAligned()
                    .color(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
                    .noShadow()
                    .tooltip(display.blocks.map { it.name }.join(text { "\n"() })),
                Widgets.createSlot(p + Point(133, 1)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).markOutput(),
            )
        }
    }

    override fun getWorkstations() = listOf(MIRAGE_FLOUR_TAG.toIngredient().matchingStacks.map { it.toEntryStack() }.toEntryIngredient())
}

object EntityTypeFairyDreamRecipeClientReiCategoryCard : BaseFairyDreamRecipeClientReiCategoryCard<EntityType<*>, EntityTypeFairyDreamRecipeReiCategoryCard.Display>(EntityTypeFairyDreamRecipeReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        FairyDreamRecipes.ENTITY_TYPE.getDisplayMap().forEach { (motif, keys) ->
            registry.add(EntityTypeFairyDreamRecipeReiCategoryCard.Display(keys.toList(), motif))
        }
    }

    override fun createCategory() = object : DisplayCategory<EntityTypeFairyDreamRecipeReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = EntityTypeFairyDreamRecipeReiCategoryCard.identifier.first
        override fun getTitle(): Text = EntityTypeFairyDreamRecipeReiCategoryCard.translation()
        override fun getIcon(): Renderer = MotifCard.ENDERMAN.createFairyItemStack().toEntryStack()
        override fun getDisplayWidth(display: EntityTypeFairyDreamRecipeReiCategoryCard.Display) = 160
        override fun getDisplayHeight() = 28
        override fun setupDisplay(display: EntityTypeFairyDreamRecipeReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(5, 5)
            val gained = MinecraftClient.getInstance().player!!.fairyDreamContainer[display.motif]
            val text = text { display.entityTypes[0].name }
                .let { if (display.entityTypes.size > 1) text { it + "..."() } else it }
                .let { if (!gained) it.darkRed else it }
            return listOf(
                Widgets.createRecipeBase(bounds),
                Widgets.createLabel(p + Point(2, 5), text)
                    .leftAligned()
                    .color(0xFF404040.toInt(), 0xFFBBBBBB.toInt())
                    .noShadow()
                    .tooltip(display.entityTypes.map { it.name }.join(text { "\n"() })),
                Widgets.createSlot(p + Point(133, 1)).entries(display.outputEntries.getOrNull(0) ?: EntryIngredient.empty()).markOutput(),
            )
        }
    }

    override fun getWorkstations() = listOf(MIRAGE_FLOUR_TAG.toIngredient().matchingStacks.map { it.toEntryStack() }.toEntryIngredient())
}
