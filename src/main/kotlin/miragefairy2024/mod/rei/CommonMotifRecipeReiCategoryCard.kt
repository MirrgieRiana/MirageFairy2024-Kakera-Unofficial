package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.fairy.AlwaysCommonMotifRecipe
import miragefairy2024.mod.fairy.BiomeCommonMotifRecipe
import miragefairy2024.mod.fairy.BiomeTagCommonMotifRecipe
import miragefairy2024.mod.fairy.CommonMotifRecipe
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getIdentifier
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.util.Single
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object CommonMotifRecipeReiCategoryCard : ReiCategoryCard<CommonMotifRecipeReiCategoryCard.Display>("common_motif_recipe", "Common Fairy", "コモン妖精") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(run {
                val motif = motifRegistry.get(tag.wrapper["Motif"].string.get()!!.toIdentifier())!!
                when (val type = tag.wrapper["Type"].string.get()) {
                    "always" -> AlwaysCommonMotifRecipe(motif)
                    "biome" -> BiomeCommonMotifRecipe(motif, tag.wrapper["Biome"].string.get()!!.let { RegistryKey.of(RegistryKeys.BIOME, it.toIdentifier()) })
                    "biome_tag" -> BiomeTagCommonMotifRecipe(motif, tag.wrapper["BiomeTag"].string.get()!!.let { TagKey.of(RegistryKeys.BIOME, it.toIdentifier()) })
                    else -> throw IllegalArgumentException(type)
                }
            })
        }, { display, tag ->
            tag.wrapper["Motif"].string.set(display.recipe.motif.getIdentifier()!!.string)
            when (display.recipe) {
                is AlwaysCommonMotifRecipe -> {
                    tag.wrapper["Type"].string.set("always")
                }

                is BiomeCommonMotifRecipe -> {
                    tag.wrapper["Type"].string.set("biome")
                    tag.wrapper["Biome"].string.set(display.recipe.biome.value.string)
                }

                is BiomeTagCommonMotifRecipe -> {
                    tag.wrapper["Type"].string.set("biome_tag")
                    tag.wrapper["BiomeTag"].string.set(display.recipe.biomeTag.id.string)
                }
            }
        }))
    }

    class Display(val recipe: CommonMotifRecipe) : BasicDisplay(
        listOf(),
        listOf(recipe.motif.createFairyItemStack()).map { it.toEntryStack().toEntryIngredient() },
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
