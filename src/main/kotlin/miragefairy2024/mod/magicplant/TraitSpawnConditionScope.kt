package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.invoke
import miragefairy2024.util.temperatureCategory
import miragefairy2024.util.text
import miragefairy2024.util.translate
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.core.Holder as RegistryEntry

// module

context(ModContext)
fun initTraitSpawnConditionScope() {
    ANYWHERE_TRANSLATION.enJa()
    OVERWORLD_TRANSLATION.enJa()
    NETHER_TRANSLATION.enJa()
    END_TRANSLATION.enJa()
    HIGH_TRANSLATION.enJa()
    MEDIUM_TRANSLATION.enJa()
    LOW_TRANSLATION.enJa()
    TEMPERATURE_TRANSLATION.enJa()
    HUMIDITY_TRANSLATION.enJa()
}


// util

object TraitSpawnConditionScope


// 特別な条件

private val ANYWHERE_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.anywhere" }, "Anywhere", "どこでも")
context(TraitSpawnConditionScope) val anywhere
    get() = object : TraitSpawnCondition {
        override val description = text { ANYWHERE_TRANSLATION() }
        override fun canSpawn(biome: RegistryEntry<Biome>) = true
    }


// ディメンション

private val OVERWORLD_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.overworld" }, "Overworld", "オーバーワールド")
private val NETHER_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.nether" }, "Nether", "ネザー")
private val END_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.end" }, "The End", "エンド")

context(TraitSpawnConditionScope) val overworld
    get() = object : TraitSpawnCondition {
        override val description = text { OVERWORLD_TRANSLATION() }
        override fun canSpawn(biome: RegistryEntry<Biome>) = biome.`is`(ConventionalBiomeTags.IN_OVERWORLD)
    }
context(TraitSpawnConditionScope) val nether
    get() = object : TraitSpawnCondition {
        override val description = text { NETHER_TRANSLATION() }
        override fun canSpawn(biome: RegistryEntry<Biome>) = biome.`is`(ConventionalBiomeTags.IN_NETHER)
    }
context(TraitSpawnConditionScope) val end
    get() = object : TraitSpawnCondition {
        override val description = text { END_TRANSLATION() }
        override fun canSpawn(biome: RegistryEntry<Biome>) = biome.`is`(ConventionalBiomeTags.IN_THE_END)
    }


// バイオーム・タグ

context(TraitSpawnConditionScope) operator fun ResourceKey<Biome>.unaryPlus() = object : TraitSpawnCondition {
    override val description = text { translate(this@unaryPlus.location().toLanguageKey("biome")) }
    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.`is`(this@unaryPlus)
}

context(TraitSpawnConditionScope) operator fun TagKey<Biome>.unaryPlus() = object : TraitSpawnCondition {
    override val description = text { this@unaryPlus.location().path() }
    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.`is`(this@unaryPlus)
}


// 気温・湿度

private val HIGH_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.high" }, "High %s", "高い%s")
private val MEDIUM_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.medium" }, "Medium %s", "普通の%s")
private val LOW_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.low" }, "Low %s", "低い%s")
private val TEMPERATURE_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.temperature" }, "Temperature", "気温")
private val HUMIDITY_TRANSLATION = Translation({ "${MirageFairy2024.MOD_ID}.trait_spawn_condition.humidity" }, "Humidity", "湿度")

context(TraitSpawnConditionScope) operator fun TemperatureCategory.unaryPlus() = object : TraitSpawnCondition {
    override val description = when (this@unaryPlus) {
        TemperatureCategory.HIGH -> text { HIGH_TRANSLATION(TEMPERATURE_TRANSLATION()) }
        TemperatureCategory.MEDIUM -> text { MEDIUM_TRANSLATION(TEMPERATURE_TRANSLATION()) }
        TemperatureCategory.LOW -> text { LOW_TRANSLATION(TEMPERATURE_TRANSLATION()) }
    }

    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.temperatureCategory == this@unaryPlus
}

context(TraitSpawnConditionScope) operator fun HumidityCategory.unaryPlus() = object : TraitSpawnCondition {
    override val description = when (this@unaryPlus) {
        HumidityCategory.HIGH -> text { HIGH_TRANSLATION(HUMIDITY_TRANSLATION()) }
        HumidityCategory.MEDIUM -> text { MEDIUM_TRANSLATION(HUMIDITY_TRANSLATION()) }
        HumidityCategory.LOW -> text { LOW_TRANSLATION(HUMIDITY_TRANSLATION()) }
    }

    override fun canSpawn(biome: RegistryEntry<Biome>) = biome.humidityCategory == this@unaryPlus
}
