package miragefairy2024.mod.fairy

import net.minecraft.registry.tag.TagKey
import net.minecraft.world.biome.Biome

val COMMON_MOTIF_RECIPES = mutableListOf<CommonMotifRecipe>()

class CommonMotifRecipe(val motif: Motif, val biome: TagKey<Biome>?)
