package miragefairy2024.client.mod.rei

import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.client.mod.FermentationBarrelScreen
import miragefairy2024.mod.rei.FermentationBarrelReiCategoryCard
import miragefairy2024.mod.rei.MirageFairy2024ReiServerPlugin

@Suppress("unused")
class MirageFairy2024ReiClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            val category = card.createCategory()
            registry.add(category)
            registry.addWorkstations(category.categoryIdentifier, *card.getWorkstations().toTypedArray())
        }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerDisplays(registry)
        }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        MirageFairy2024ReiServerPlugin().registerItemComparators(registry)
    }

    override fun registerScreens(registry: ScreenRegistry) {
        registry.registerContainerClickArea(Rectangle(77 - 1, 27, 22 + 2, 16 + 2), FermentationBarrelScreen::class.java, FermentationBarrelReiCategoryCard.identifier.first)
    }
}
