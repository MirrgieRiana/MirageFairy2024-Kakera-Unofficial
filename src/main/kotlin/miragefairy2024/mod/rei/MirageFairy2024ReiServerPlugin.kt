package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation

abstract class ReiCategoryCard<D : BasicDisplay>(
    val path: String,
    enName: String,
    jaName: String,
) {
    companion object {
        val entries = listOf(
            WorldGenTraitReiCategoryCard,
            MagicPlantCropReiCategoryCard,
            FairyQuestRecipeReiCategoryCard,
        )
    }

    val translation = Translation({ "category.rei.${MirageFairy2024.modId}.$path" }, enName, jaName)
    val identifier: CategoryIdentifier<D> by lazy { CategoryIdentifier.of(MirageFairy2024.modId, "plugins/$path") }
    abstract val serializer: BasicDisplay.Serializer<D>
}

@Suppress("unused")
class MirageFairy2024ReiServerPlugin : REIServerPlugin {
    override fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        ReiCategoryCard.entries.forEach { card ->
            fun <D : BasicDisplay> f(card: ReiCategoryCard<D>) {
                registry.register(card.identifier, card.serializer)
            }
            f(card)
        }
    }
}
