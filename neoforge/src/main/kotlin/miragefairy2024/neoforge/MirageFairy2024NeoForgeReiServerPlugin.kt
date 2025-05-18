package miragefairy2024.neoforge

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import me.shedaniel.rei.forge.REIPluginCommon
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.fairy.getIdentifier
import miragefairy2024.mod.rei.ReiCategoryCard
import miragefairy2024.util.string

@REIPluginCommon
@Suppress("unused")
class MirageFairy2024NeoForgeReiServerPlugin : REIServerPlugin {
    override fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        ReiCategoryCard.entries.forEach { card ->
            fun <D : BasicDisplay> f(card: ReiCategoryCard<D>) {
                registry.register(card.identifier.first, card.serializer.first)
            }
            f(card)
        }
    }

    override fun registerItemComparators(registry: ItemComparatorRegistry) {
        registry.register({ context, stack ->
            if (context.isExact) {
                EntryComparator.itemComponents().hash(context, stack)
            } else {
                stack.getFairyMotif()?.getIdentifier()?.string?.hashCode()?.toLong() ?: 1L
            }
        }, FairyCard.item())
    }
}
