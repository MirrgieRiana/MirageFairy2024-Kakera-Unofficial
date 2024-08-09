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
import miragefairy2024.mod.magicplant.magicPlantCards
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.magicplant.traitRegistry
import miragefairy2024.mod.rei.TraitReiCategoryCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.formatted
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer
import net.minecraft.text.Text

object TraitSpawnClientReiCategoryCard : ClientReiCategoryCard<TraitReiCategoryCard.Display>(TraitReiCategoryCard) {
    override fun registerDisplays(registry: DisplayRegistry) {
        traitRegistry.entrySet.forEach { (_, trait) ->
            val seedItemStacks = magicPlantCards.filter { trait in it.possibleTraits }.map { it.item.createItemStack() }
            registry.add(TraitReiCategoryCard.Display(seedItemStacks.map { it.toEntryStack().toEntryIngredient() }, trait))
        }
    }

    override fun createCategory() = object : DisplayCategory<TraitReiCategoryCard.Display> {
        override fun getCategoryIdentifier() = TraitReiCategoryCard.identifier.first
        override fun getTitle(): Text = TraitReiCategoryCard.translation()
        override fun getIcon(): Renderer = MirageFlowerCard.item.createItemStack().toEntryStack()
        override fun getDisplayWidth(display: TraitReiCategoryCard.Display) = 3 + 2 + 18 * 9 + 2 + 3
        override fun getDisplayHeight() = 140
        override fun setupDisplay(display: TraitReiCategoryCard.Display, bounds: Rectangle): List<Widget> {
            val p = bounds.location + Point(3, 3)
            var y = 0
            val widgets = mutableListOf<Widget>()

            widgets += Widgets.createRecipeBase(bounds)

            // 特性カード
            widgets += run {
                val y2 = y
                Widgets.createDrawableWidget { graphics, _, _, _ ->
                    TooltipBackgroundRenderer.render(graphics, p.x + 6, p.y + y2 + 6, bounds.width - 18, 47, 0)
                }
            }

            // 特性名
            widgets += Widgets.createLabel(p + Point(18 * 9 / 2, y + 6), text { display.trait.getName().formatted(display.trait.color) }).centered()

            y += 19

            // 特性アイコン
            widgets += Widgets.createTexturedWidget(display.trait.texture, bounds.centerX - 16, p.y + y, 0F, 0F, 32, 32, 32, 32)

            y += 40

            // 出力スロット背景
            repeat(9) { c ->
                widgets += Widgets.createSlotBackground(p + Point(2 + 1 + 18 * c, y + 1))
            }

            // 出力アイテム
            display.inputEntries.forEachIndexed { i, input ->
                widgets += Widgets.createSlot(p + Point(2 + 1 + 18 * (i % 9), y + 1 + 18 * (i / 9))).entries(input).disableBackground().markInput()
            }

            y += 18 + 3

            // 出現条件
            display.trait.spawnSpecs.forEach { spawnSpec ->

                // レベル
                widgets += Widgets.createLabel(p + Point(30, y), text { spawnSpec.level.toString(2)() }).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow().rightAligned()

                // 出現頻度
                val rarityText = when (spawnSpec.rarity) {
                    TraitSpawnRarity.ALWAYS -> text { "100%"() }
                    TraitSpawnRarity.COMMON -> text { ">99%"() }
                    TraitSpawnRarity.NORMAL -> text { "<90%"() }
                    TraitSpawnRarity.RARE -> text { "<8%"() }
                    TraitSpawnRarity.S_RARE -> text { "<1%"() }
                }
                widgets += Widgets.createLabel(p + Point(50, y), rarityText).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow().centered()

                // 条件
                widgets += Widgets.createLabel(p + Point(70, y), spawnSpec.condition.description).color(0xFF404040.toInt(), 0xFFBBBBBB.toInt()).noShadow().leftAligned()

                y += 12

            }

            return widgets
        }
    }
}
