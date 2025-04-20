package miragefairy2024.mod.fairy

import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome

val COMMON_MOTIF_RECIPES = mutableListOf<CommonMotifRecipe>()

sealed class CommonMotifRecipe(val motif: Motif)
class AlwaysCommonMotifRecipe(motif: Motif) : CommonMotifRecipe(motif)
class BiomeCommonMotifRecipe(motif: Motif, val biome: ResourceKey<Biome>) : CommonMotifRecipe(motif)
class BiomeTagCommonMotifRecipe(motif: Motif, val biomeTag: TagKey<Biome>) : CommonMotifRecipe(motif)
