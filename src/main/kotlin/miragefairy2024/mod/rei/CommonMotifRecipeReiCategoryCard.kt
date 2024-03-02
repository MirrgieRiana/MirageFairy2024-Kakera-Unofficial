package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.fairy.CommonMotifRecipe
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.getIdentifier
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.mod.fairy.setFairyMotif
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object CommonMotifRecipeReiCategoryCard : ReiCategoryCard<CommonMotifRecipeReiCategoryCard.Display>("common_motif_recipe", "Common Fairy", "コモン妖精") {
    override val serializer: BasicDisplay.Serializer<Display> by lazy {
        BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
                CommonMotifRecipe(
                    motifRegistry.get(tag.wrapper["Motif"].string.get()!!.toIdentifier())!!,
                    tag.wrapper["Biome"].string.get()?.let { TagKey.of(RegistryKeys.BIOME, it.toIdentifier()) },
                )
            )
        }, { display, tag ->
            tag.wrapper["Motif"].string.set(display.recipe.motif.getIdentifier()!!.string)
            tag.wrapper["Biome"].string.set(display.recipe.biome?.id?.string)
        })
    }

    class Display(val recipe: CommonMotifRecipe) : BasicDisplay(listOf(), listOf(FairyCard.item.createItemStack().also {
        it.setFairyMotif(recipe.motif)
    }).map { it.toEntryStack().toEntryIngredient() }) {
        override fun getCategoryIdentifier() = identifier
    }
}
