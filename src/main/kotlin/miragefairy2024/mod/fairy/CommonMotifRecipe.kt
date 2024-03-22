package miragefairy2024.mod.fairy

import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.TagKey
import net.minecraft.world.biome.Biome

val COMMON_MOTIF_RECIPES = mutableListOf<CommonMotifRecipe>()

sealed class CommonMotifRecipe(val motif: Motif)
class AlwaysCommonMotifRecipe(motif: Motif) : CommonMotifRecipe(motif)
class BiomeCommonMotifRecipe(motif: Motif, val biome: RegistryKey<Biome>) : CommonMotifRecipe(motif)
class BiomeTagCommonMotifRecipe(motif: Motif, val biomeTag: TagKey<Biome>) : CommonMotifRecipe(motif)
