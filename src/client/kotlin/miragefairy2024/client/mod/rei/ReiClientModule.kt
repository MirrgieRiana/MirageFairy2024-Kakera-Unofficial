package miragefairy2024.client.mod.rei

import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.rei.ReiCategoryCard

abstract class ClientReiCategoryCard<D : BasicDisplay>(val parent: ReiCategoryCard<D>) {
    companion object {
        val entries = listOf(
            WorldGenTraitClientReiCategoryCard,
            MagicPlantCropClientReiCategoryCard,
            FairyQuestRecipeClientReiCategoryCard,
            CommonMotifRecipeClientReiCategoryCard,
        )
    }

    abstract fun registerDisplays(registry: DisplayRegistry)
    abstract fun createCategory(): DisplayCategory<D>
}
