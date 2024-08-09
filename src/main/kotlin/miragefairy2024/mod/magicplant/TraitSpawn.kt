package miragefairy2024.mod.magicplant

import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.temperatureCategory
import miragefairy2024.util.text
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.world.biome.Biome

// api

class TraitSpawnSpec(val condition: TraitSpawnCondition, val rarity: TraitSpawnRarity, val level: Int)

interface TraitSpawnCondition {
    val description: Text
    fun canSpawn(biome: RegistryEntry<Biome>): Boolean
}

enum class TraitSpawnRarity {
    /** 必ず付与される。 */
    ALWAYS,

    /** 1%の確率で選ばれるC欠損テーブルに乗る。 */
    COMMON,

    /** 90%の確率で選ばれるN獲得テーブルに乗る。 */
    NORMAL,

    /** 8%の確率で選ばれるR獲得テーブルに乗る。 */
    RARE,

    /** 1%の確率で選ばれるSR獲得テーブルに乗る。 */
    S_RARE,
}


// util

object TraitSpawnConditionScope

context(TraitSpawnConditionScope) val always
    get() = object : TraitSpawnCondition {
        override val description = text { "always"() } // TODO
        override fun canSpawn(biome: RegistryEntry<Biome>) = true
    }

context(TraitSpawnConditionScope) val overworld get() = +ConventionalBiomeTags.IN_OVERWORLD
context(TraitSpawnConditionScope) val nether get() = +ConventionalBiomeTags.IN_NETHER
context(TraitSpawnConditionScope) val end get() = +ConventionalBiomeTags.IN_THE_END

context(TraitSpawnConditionScope) operator fun RegistryKey<Biome>.unaryPlus() = object : TraitSpawnCondition {
    override val description = text { "in ${this@unaryPlus.value.path}"() } // TODO
    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.matchesKey(this@unaryPlus)
}

context(TraitSpawnConditionScope) operator fun TagKey<Biome>.unaryPlus() = object : TraitSpawnCondition {
    override val description = text { "in ${this@unaryPlus.id.path}"() } // TODO
    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.isIn(this@unaryPlus)
}

context(TraitSpawnConditionScope) operator fun TemperatureCategory.unaryPlus() = object : TraitSpawnCondition {
    override val description = text { "${this@unaryPlus.name} temperature"() } // TODO
    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.temperatureCategory == this@unaryPlus
}

context(TraitSpawnConditionScope) operator fun HumidityCategory.unaryPlus() = object : TraitSpawnCondition {
    override val description = text { "${this@unaryPlus.name} humidity"() } // TODO
    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.humidityCategory == this@unaryPlus
}
