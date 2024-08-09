package miragefairy2024.mod.magicplant

import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.temperatureCategory
import miragefairy2024.util.text
import net.minecraft.block.Block
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome

// api

val worldGenTraitRecipeRegistry = mutableMapOf<Block, MutableList<WorldGenTraitRecipe>>()

class WorldGenTraitRecipe(
    val block: Block,
    val rarity: Rarity,
    val trait: Trait,
    val level: Int,
    val condition: Condition
) {

    enum class Rarity {
        /** 必ず付与される。 */
        A,

        /** 1%の確率で選ばれるC欠損テーブルに乗る。 */
        C,

        /** 90%の確率で選ばれるN獲得テーブルに乗る。 */
        N,

        /** 8%の確率で選ばれるR獲得テーブルに乗る。 */
        R,

        /** 1%の確率で選ばれるSR獲得テーブルに乗る。 */
        S,
    }

    interface Condition {
        val description: Text
        fun canSpawn(world: World, blockPos: BlockPos): Boolean

        object Always : Condition {
            override val description = text { "always"() } // TODO
            override fun canSpawn(world: World, blockPos: BlockPos) = true
        }

        class InBiome(val biomeTag: TagKey<Biome>) : Condition {
            override val description = text { "in ${biomeTag.id.path}"() } // TODO
            override fun canSpawn(world: World, blockPos: BlockPos) = world.getBiome(blockPos).isIn(biomeTag)
        }

        class NotInBiome(val biomeTag: TagKey<Biome>) : Condition {
            override val description = text { "not in ${biomeTag.id.path}"() } // TODO
            override fun canSpawn(world: World, blockPos: BlockPos) = !world.getBiome(blockPos).isIn(biomeTag)
        }

        class Temperature(val temperature: TemperatureCategory) : Condition {
            override val description = text { "${temperature.name} temperature"() } // TODO
            override fun canSpawn(world: World, blockPos: BlockPos) = world.getBiome(blockPos).temperatureCategory == temperature
        }

        class Humidity(val humidity: HumidityCategory) : Condition {
            override val description = text { "${humidity.name} humidity"() } // TODO
            override fun canSpawn(world: World, blockPos: BlockPos) = world.getBiome(blockPos).humidityCategory == humidity
        }
    }

}


// util

fun registerWorldGenTraitRecipe(recipe: WorldGenTraitRecipe) {
    worldGenTraitRecipeRegistry.getOrPut(recipe.block) { mutableListOf() } += recipe
}

class WorldGenTraitRecipeInitScope(val block: Block) {

    fun registerWorldGenTraitRecipe(pattern: String, traitCard: TraitCard, condition: WorldGenTraitRecipe.Condition = WorldGenTraitRecipe.Condition.Always) {
        pattern.forEachIndexed { i, ch ->
            val rarity = when (ch) {
                '.' -> return@forEachIndexed
                'A' -> WorldGenTraitRecipe.Rarity.A
                'C' -> WorldGenTraitRecipe.Rarity.C
                'N' -> WorldGenTraitRecipe.Rarity.N
                'R' -> WorldGenTraitRecipe.Rarity.R
                'S' -> WorldGenTraitRecipe.Rarity.S
                else -> throw IllegalArgumentException()
            }
            val level = 1 shl (pattern.length - 1 - i)
            registerWorldGenTraitRecipe(WorldGenTraitRecipe(block, rarity, traitCard.trait, level, condition))
        }
    }

}
