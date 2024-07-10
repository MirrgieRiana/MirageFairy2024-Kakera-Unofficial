package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa

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
            CommonMotifRecipeReiCategoryCard,
            ItemFairyDreamRecipeReiCategoryCard,
            BlockFairyDreamRecipeReiCategoryCard,
            EntityTypeFairyDreamRecipeReiCategoryCard,
            FairyFamilyReiCategoryCard,
        )
    }

    val translation = Translation({ "category.rei.${MirageFairy2024.modId}.$path" }, enName, jaName)
    val identifier: CategoryIdentifier<D> by lazy { CategoryIdentifier.of(MirageFairy2024.modId, "plugins/$path") }
    abstract val serializer: BasicDisplay.Serializer<D>
}

val COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION = Translation({ "gui.miragefairy2024.common_motif_recipe.always" }, "Always", "常時")

fun initReiModule() {
    ReiCategoryCard.entries.forEach { card ->
        card.translation.enJa()
    }

    COMMON_MOTIF_RECIPE_ALWAYS_TRANSLATION.enJa()
}
