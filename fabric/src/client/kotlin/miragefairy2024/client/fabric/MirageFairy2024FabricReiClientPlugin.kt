package miragefairy2024.client.fabric

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import miragefairy2024.client.mod.rei.ClientReiCategoryCard
import miragefairy2024.fabric.MirageFairy2024FabricReiServerPlugin

@Suppress("unused")
class MirageFairy2024FabricReiClientPlugin : REIClientPlugin {
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
        MirageFairy2024FabricReiServerPlugin().registerItemComparators(registry)
    }

    override fun registerScreens(registry: ScreenRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerScreens(registry)
        }
    }
}
