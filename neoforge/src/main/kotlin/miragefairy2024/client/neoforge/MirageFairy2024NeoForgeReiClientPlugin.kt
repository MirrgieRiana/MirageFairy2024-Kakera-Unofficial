package miragefairy2024.client.neoforge

import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.forge.REIPluginClient
import miragefairy2024.client.mod.rei.ClientReiCategoryCard
import miragefairy2024.neoforge.MirageFairy2024NeoForgeReiServerPlugin

@REIPluginClient
@Suppress("unused")
class MirageFairy2024NeoForgeReiClientPlugin : REIClientPlugin {
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
        MirageFairy2024NeoForgeReiServerPlugin().registerItemComparators(registry)
    }

    override fun registerScreens(registry: ScreenRegistry) {
        ClientReiCategoryCard.entries.forEach { card ->
            card.registerScreens(registry)
        }
    }
}
