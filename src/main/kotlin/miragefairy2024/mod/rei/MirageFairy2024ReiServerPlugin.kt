package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.getFairyMotifId
import miragefairy2024.util.string

@Suppress("unused")
class MirageFairy2024ReiServerPlugin : REIServerPlugin {
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
                EntryComparator.itemNbt().hash(context, stack)
            } else {
                stack.getFairyMotifId()?.string?.hashCode()?.toLong() ?: 1L
            }
        }, FairyCard.item)
    }
}
