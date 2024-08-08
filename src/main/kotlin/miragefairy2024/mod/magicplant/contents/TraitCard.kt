package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.TraitSpawnCondition
import miragefairy2024.mod.magicplant.TraitSpawnConditionScope
import miragefairy2024.mod.magicplant.TraitSpawnRarity
import miragefairy2024.mod.magicplant.TraitSpawnSpec
import miragefairy2024.mod.magicplant.always
import miragefairy2024.mod.magicplant.enJa
import miragefairy2024.mod.magicplant.end
import miragefairy2024.mod.magicplant.nether
import miragefairy2024.mod.magicplant.traitRegistry
import miragefairy2024.mod.magicplant.unaryPlus
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.register
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeKeys

class TraitCard private constructor(
    path: String,
    private val sortKey: String,
    val enName: String,
    val jaName: String,
    private val traitFactorCard: TraitFactorCard,
    private val traitEffectKeyCard: TraitEffectKeyCard,
    private val spawnSpecConfigurator: MutableList<TraitSpawnSpec>.() -> Unit,
) {
    companion object {
        val entries = mutableListOf<TraitCard>()
        operator fun TraitCard.not() = also { entries += this }

        val ETHER_RESPIRATION = !TraitCard("ether_respiration", "0nutrition", "Ether Respiration", "エーテル呼吸", TraitFactorCard.ALWAYS, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.ALWAYS)
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.IN_THE_END }
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.S_RARE)
        }
        val PHOTOSYNTHESIS = !TraitCard("photosynthesis", "0nutrition", "Photosynthesis", "光合成", TraitFactorCard.LIGHT, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.SNOWY }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.DESERT }
            register("0010", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.IN_OVERWORLD }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.IN_OVERWORLD }
        }
        val PHAEOSYNTHESIS = !TraitCard("phaeosynthesis", "0nutrition", "Phaeosynthesis", "闇合成", TraitFactorCard.DARKNESS, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.IN_NETHER }
            register("0100", TraitSpawnRarity.RARE) { +BiomeKeys.SOUL_SAND_VALLEY }
            register("0010", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.IN_NETHER }
            register("0001", TraitSpawnRarity.NORMAL) { +BiomeKeys.DEEP_DARK }
        }
        val OSMOTIC_ABSORPTION = !TraitCard("osmotic_absorption", "0nutrition", "Osmotic Absorption", "浸透吸収", TraitFactorCard.FLOOR_MOISTURE, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.COMMON) { +BiomeKeys.SOUL_SAND_VALLEY }
            register("0100", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.PLAINS }
            register("0010", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.IN_OVERWORLD }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.IN_OVERWORLD }
        }
        val CRYSTAL_ABSORPTION = !TraitCard("crystal_absorption", "0nutrition", "Crystal Absorption", "鉱物吸収", TraitFactorCard.FLOOR_CRYSTAL_ERG, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.BEACH }
            register("0001", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.IN_NETHER }
        }

        val AIR_ADAPTATION = !TraitCard("air_adaptation", "1atmosphere", "Air Adaptation", "空気適応", TraitFactorCard.ALWAYS, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.ALWAYS)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.S_RARE)
        }
        val COLD_ADAPTATION = !TraitCard("cold_adaptation", "3biome", "Cold Adaptation", "寒冷適応", TraitFactorCard.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.LOW }
            register("0100", TraitSpawnRarity.COMMON) { +TemperatureCategory.LOW }
            register("0010", TraitSpawnRarity.RARE) { +TemperatureCategory.LOW }
            register("0001", TraitSpawnRarity.NORMAL) { +TemperatureCategory.LOW }
        }
        val WARM_ADAPTATION = !TraitCard("warm_adaptation", "3biome", "Warm Adaptation", "温暖適応", TraitFactorCard.MEDIUM_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.MEDIUM }
            register("0100", TraitSpawnRarity.COMMON) { +TemperatureCategory.MEDIUM }
            register("0010", TraitSpawnRarity.RARE) { +TemperatureCategory.MEDIUM }
            register("0001", TraitSpawnRarity.NORMAL) { +TemperatureCategory.MEDIUM }
        }
        val HOT_ADAPTATION = !TraitCard("hot_adaptation", "3biome", "Hot Adaptation", "熱帯適応", TraitFactorCard.HIGH_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.HIGH }
            register("0100", TraitSpawnRarity.COMMON) { +TemperatureCategory.HIGH }
            register("0010", TraitSpawnRarity.RARE) { +TemperatureCategory.HIGH }
            register("0001", TraitSpawnRarity.NORMAL) { +TemperatureCategory.HIGH }
        }
        val ARID_ADAPTATION = !TraitCard("arid_adaptation", "3biome", "Arid Adaptation", "乾燥適応", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.COMMON) { +HumidityCategory.LOW }
            register("0010", TraitSpawnRarity.RARE) { +HumidityCategory.LOW }
            register("0001", TraitSpawnRarity.NORMAL) { +HumidityCategory.LOW }
        }
        val MESIC_ADAPTATION = !TraitCard("mesic_adaptation", "3biome", "Mesic Adaptation", "中湿適応", TraitFactorCard.MEDIUM_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.MEDIUM }
            register("0100", TraitSpawnRarity.COMMON) { +HumidityCategory.MEDIUM }
            register("0010", TraitSpawnRarity.RARE) { +HumidityCategory.MEDIUM }
            register("0001", TraitSpawnRarity.NORMAL) { +HumidityCategory.MEDIUM }
        }
        val HUMID_ADAPTATION = !TraitCard("humid_adaptation", "3biome", "Humid Adaptation", "湿潤適応", TraitFactorCard.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.HIGH }
            register("0100", TraitSpawnRarity.COMMON) { +HumidityCategory.HIGH }
            register("0010", TraitSpawnRarity.RARE) { +HumidityCategory.HIGH }
            register("0001", TraitSpawnRarity.NORMAL) { +HumidityCategory.HIGH }
        }

        val SEEDS_PRODUCTION = !TraitCard("seeds_production", "4production", "Seeds Production", "種子生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val FRUITS_PRODUCTION = !TraitCard("fruits_production", "4production", "Fruits Production", "果実生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val LEAVES_PRODUCTION = !TraitCard("leaves_production", "4production", "Leaves Production", "葉面生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.LEAVES_PRODUCTION) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val RARE_PRODUCTION = !TraitCard("rare_production", "4production", "Rare Production", "希少品生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.RARE_PRODUCTION) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val EXPERIENCE_PRODUCTION = !TraitCard("experience_production", "4production", "Xp Production", "経験値生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION) {
            register("1000", TraitSpawnRarity.S_RARE)
            //register("0100", )
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.S_RARE)
        }

        val FAIRY_BLESSING = !TraitCard("fairy_blessing", "5skill", "Fairy Blessing", "妖精の祝福", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR) {
            //register("1000", )
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }

        val FOUR_LEAFED = !TraitCard("four_leafed", "6part", "Four-leafed", "四つ葉", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR) {
            register("1000", TraitSpawnRarity.NORMAL) { +BiomeKeys.WARPED_FOREST }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FLORAL }
            register("0010", TraitSpawnRarity.S_RARE)
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.PLAINS }
        }
        val NODED_STEM = !TraitCard("noded_stem", "6part", "Noded Stem", "節状の茎", TraitFactorCard.ALWAYS, TraitEffectKeyCard.GROWTH_BOOST) {
            //r("1000", )
            register("0100", TraitSpawnRarity.COMMON) { +BiomeKeys.BAMBOO_JUNGLE }
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.BEACH }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.JUNGLE }
        }
        val FRUIT_OF_KNOWLEDGE = !TraitCard("fruit_of_knowledge", "6part", "Fruit of Knowledge", "知識の果実", TraitFactorCard.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.HIGH }
            register("0100", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.JUNGLE }
            register("0010", TraitSpawnRarity.RARE) { +BiomeKeys.WARPED_FOREST }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FOREST }
        }
        val GOLDEN_APPLE = !TraitCard("golden_apple", "6part", "Golden Apple", "金のリンゴ", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR) {
            register("1000", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.FOREST }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FOREST }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.JUNGLE }
            register("0001", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.FOREST }
        }
        val SPINY_LEAVES = !TraitCard("spiny_leaves", "6part", "Spiny Leaves", "棘状の葉", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.MESA }
            register("0010", TraitSpawnRarity.NORMAL) { nether }
            register("0001", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.DESERT }
        }
        val DESERT_GEM = !TraitCard("desert_gem", "6part", "Desert Gem", "砂漠の宝石", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.PRODUCTION_BOOST) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.DESERT }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MESA }
            register("0001", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.DESERT }
        }
        val HEATING_MECHANISM = !TraitCard("heating_mechanism", "6part", "Heating Mechanism", "発熱機構", TraitFactorCard.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.LOW }
            register("0100", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.SNOWY }
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.ICY }
            register("0001", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.TAIGA }
        }
        val WATERLOGGING_TOLERANCE = !TraitCard("waterlogging_tolerance", "6part", "Waterlogging Tolerance", "浸水耐性", TraitFactorCard.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.HIGH }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.RIVER }
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.SWAMP }
            register("0001", TraitSpawnRarity.NORMAL) { +BiomeKeys.LUSH_CAVES }
        }
        val ADVERSITY_FLOWER = !TraitCard("adversity_flower", "6part", "Adversity Flower", "高嶺の花", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION) {
            register("1000", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.EXTREME_HILLS }
        }
        val FLESHY_LEAVES = !TraitCard("fleshy_leaves", "6part", "Fleshy Leaves", "肉厚の葉", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.LEAVES_PRODUCTION) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.SAVANNA }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.DESERT }
            register("0001", TraitSpawnRarity.RARE) { nether }
        }
        val NATURAL_ABSCISSION = !TraitCard("natural_abscission", "6part", "Natural Abscission", "自然落果", TraitFactorCard.ALWAYS, TraitEffectKeyCard.NATURAL_ABSCISSION) {
            register("1000", TraitSpawnRarity.S_RARE)
            register("0100", TraitSpawnRarity.NORMAL) { +BiomeKeys.ICE_SPIKES }
            register("0010", TraitSpawnRarity.NORMAL) { +BiomeKeys.BASALT_DELTAS }
            register("0001", TraitSpawnRarity.NORMAL) { +BiomeKeys.DRIPSTONE_CAVES }
        }
        val CARNIVOROUS_PLANT = !TraitCard("carnivorous_plant", "6part", "Carnivorous Plant", "食虫植物", TraitFactorCard.OUTDOOR, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.S_RARE) { nether }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.SWAMP }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.JUNGLE }
        }
        val ETHER_PREDATION = !TraitCard("ether_predation", "6part", "Ether Predation", "エーテル捕食", TraitFactorCard.ALWAYS, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.S_RARE) { end }
            register("0100", TraitSpawnRarity.NORMAL) { end }
            register("0010", TraitSpawnRarity.RARE) { end }
            register("0001", TraitSpawnRarity.S_RARE) { end }
        }
        val PAVEMENT_FLOWERS = !TraitCard("pavement_flowers", "6part", "Pavement Flowers", "アスファルトに咲く花", TraitFactorCard.FLOOR_HARDNESS, TraitEffectKeyCard.NUTRITION) {
            register("1000", TraitSpawnRarity.S_RARE) { nether }
            register("0100", TraitSpawnRarity.COMMON) { nether }
            register("0010", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.CAVES }
            register("0001", TraitSpawnRarity.RARE) { +BiomeKeys.BASALT_DELTAS }
        }
        val PROSPERITY_OF_SPECIES = !TraitCard("prosperity_of_species", "6part", "Prosperity of Species", "種の繁栄", TraitFactorCard.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.MEDIUM }
            register("0100", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.PLAINS }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FOREST }
            register("0001", TraitSpawnRarity.S_RARE) { +TemperatureCategory.MEDIUM }
        }
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    val trait: Trait = object : Trait(traitEffectKeyCard.color, sortKey) {
        override fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects? {
            val factor = traitFactorCard.traitFactor.getFactor(world, blockPos)
            return if (factor != 0.0) {
                val traitEffects = MutableTraitEffects()
                traitEffects[traitEffectKeyCard.traitEffectKey] = traitEffectKeyCard.traitEffectKey.getValue(level) * factor
                traitEffects
            } else {
                null
            }
        }

        override val spawnSpecs = mutableListOf<TraitSpawnSpec>().also { spawnSpecConfigurator(it) }
    }

}

private fun MutableList<TraitSpawnSpec>.register(binary: String, rarity: TraitSpawnRarity, conditionGetter: context(TraitSpawnConditionScope)() -> TraitSpawnCondition = { always }) {
    this += TraitSpawnSpec(conditionGetter(TraitSpawnConditionScope), rarity, binary.toInt(2))
}


context(ModContext)
fun initTraitCard() {
    TraitCard.entries.forEach { card ->
        card.trait.register(traitRegistry, card.identifier)
        card.trait.enJa(card.enName, card.jaName)
    }
}
