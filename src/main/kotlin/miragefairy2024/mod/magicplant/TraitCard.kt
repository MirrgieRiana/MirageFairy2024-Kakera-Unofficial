package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.getCrystalErg
import miragefairy2024.util.getMoisture
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.temperatureCategory
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.Heightmap
import kotlin.math.pow

// TraitCard

enum class TraitCard(
    path: String,
    sortKey: String,
    val enName: String,
    val jaName: String,
    factor: TraitFactor,
    traitEffectKeyCard: TraitEffectKeyCard,
) {
    ETHER_RESPIRATION("ether_respiration", "0nutrition", "Ether Respiration", "エーテル呼吸", TraitFactors.ALWAYS, TraitEffectKeyCard.NUTRITION),
    PHOTOSYNTHESIS("photosynthesis", "0nutrition", "Photosynthesis", "光合成", TraitFactors.LIGHT, TraitEffectKeyCard.NUTRITION),
    PHAEOSYNTHESIS("phaeosynthesis", "0nutrition", "Phaeosynthesis", "闇合成", TraitFactors.DARKNESS, TraitEffectKeyCard.NUTRITION),
    OSMOTIC_ABSORPTION("osmotic_absorption", "0nutrition", "Osmotic Absorption", "浸透吸収", TraitFactors.FLOOR_MOISTURE, TraitEffectKeyCard.NUTRITION),
    CRYSTAL_ABSORPTION("crystal_absorption", "0nutrition", "Crystal Absorption", "鉱物吸収", TraitFactors.FLOOR_CRYSTAL_ERG, TraitEffectKeyCard.NUTRITION),

    AIR_ADAPTATION("air_adaptation", "1atmosphere", "Air Adaptation", "空気適応", TraitFactors.ALWAYS, TraitEffectKeyCard.ENVIRONMENT),
    COLD_ADAPTATION("cold_adaptation", "3biome", "Cold Adaptation", "寒冷適応", TraitFactors.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    WARM_ADAPTATION("warm_adaptation", "3biome", "Warm Adaptation", "温暖適応", TraitFactors.MEDIUM_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    HOT_ADAPTATION("hot_adaptation", "3biome", "Hot Adaptation", "熱帯適応", TraitFactors.HIGH_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    ARID_ADAPTATION("arid_adaptation", "3biome", "Arid Adaptation", "乾燥適応", TraitFactors.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    MESIC_ADAPTATION("mesic_adaptation", "3biome", "Mesic Adaptation", "中湿適応", TraitFactors.MEDIUM_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    HUMID_ADAPTATION("humid_adaptation", "3biome", "Humid Adaptation", "湿潤適応", TraitFactors.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),

    SEEDS_PRODUCTION("seeds_production", "4production", "Seeds Production", "種子生成", TraitFactors.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION),
    FRUITS_PRODUCTION("fruits_production", "4production", "Fruits Production", "果実生成", TraitFactors.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION),
    LEAVES_PRODUCTION("leaves_production", "4production", "Leaves Production", "葉面生成", TraitFactors.ALWAYS, TraitEffectKeyCard.LEAVES_PRODUCTION),
    RARE_PRODUCTION("rare_production", "4production", "Rare Production", "希少品生成", TraitFactors.ALWAYS, TraitEffectKeyCard.RARE_PRODUCTION),
    EXPERIENCE_PRODUCTION("experience_production", "4production", "Xp Production", "経験値生成", TraitFactors.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION),

    FAIRY_BLESSING("fairy_blessing", "5skill", "Fairy Blessing", "妖精の祝福", TraitFactors.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR),

    FOUR_LEAFED("four_leafed", "6part", "Four-leafed", "四つ葉", TraitFactors.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR),
    NODED_STEM("noded_stem", "6part", "Noded Stem", "節状の茎", TraitFactors.ALWAYS, TraitEffectKeyCard.GROWTH_BOOST),
    FRUIT_OF_KNOWLEDGE("fruit_of_knowledge", "6part", "Fruit of Knowledge", "知識の果実", TraitFactors.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION),
    GOLDEN_APPLE("golden_apple", "6part", "Golden Apple", "金のリンゴ", TraitFactors.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR),
    SPINY_LEAVES("spiny_leaves", "6part", "Spiny Leaves", "棘状の葉", TraitFactors.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    DESERT_GEM("desert_gem", "6part", "Desert Gem", "砂漠の宝石", TraitFactors.LOW_HUMIDITY, TraitEffectKeyCard.PRODUCTION_BOOST),
    HEATING_MECHANISM("heating_mechanism", "6part", "Heating Mechanism", "発熱機構", TraitFactors.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    WATERLOGGING_TOLERANCE("waterlogging_tolerance", "6part", "Waterlogging Tolerance", "浸水耐性", TraitFactors.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    ADVERSITY_FLOWER("adversity_flower", "6part", "Adversity Flower", "高嶺の花", TraitFactors.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION),
    FLESHY_LEAVES("fleshy_leaves", "6part", "Fleshy Leaves", "肉厚の葉", TraitFactors.LOW_HUMIDITY, TraitEffectKeyCard.LEAVES_PRODUCTION),
    NATURAL_ABSCISSION("natural_abscission", "6part", "Natural Abscission", "自然落果", TraitFactors.ALWAYS, TraitEffectKeyCard.NATURAL_ABSCISSION),
    CARNIVOROUS_PLANT("carnivorous_plant", "6part", "Carnivorous Plant", "食虫植物", TraitFactors.OUTDOOR, TraitEffectKeyCard.NUTRITION),
    ETHER_PREDATION("ether_predation", "6part", "Ether Predation", "エーテル捕食", TraitFactors.ALWAYS, TraitEffectKeyCard.NUTRITION),
    PAVEMENT_FLOWERS("pavement_flowers", "6part", "Pavement Flowers", "アスファルトに咲く花", TraitFactors.FLOOR_HARDNESS, TraitEffectKeyCard.NUTRITION),
    PROSPERITY_OF_SPECIES("prosperity_of_species", "6part", "Prosperity of Species", "種の繁栄", TraitFactors.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val trait = CompoundTrait(sortKey, factor, traitEffectKeyCard)
}


// TraitEffectKey

enum class TraitEffectKeyCard(
    path: String,
    val enName: String,
    val jaName: String,
    val color: Formatting,
    isLogScale: Boolean,
) {
    NUTRITION("nutrition", "NTR", "栄養値", Formatting.AQUA, false),
    ENVIRONMENT("environment", "ENV", "環境値", Formatting.GREEN, false),
    GROWTH_BOOST("growth_boost", "GRW", "成長速度", Formatting.DARK_BLUE, false),
    SEEDS_PRODUCTION("seeds_production", "SEED", "種子生成", Formatting.RED, false),
    FRUITS_PRODUCTION("fruits_production", "FRUIT", "果実生成", Formatting.LIGHT_PURPLE, false),
    LEAVES_PRODUCTION("leaves_production", "LEAF", "葉面生成", Formatting.DARK_GREEN, false),
    RARE_PRODUCTION("rare_production", "RARE", "希少品生成", Formatting.GOLD, false),
    PRODUCTION_BOOST("production_boost", "PRD", "生産能力", Formatting.DARK_RED, false),
    EXPERIENCE_PRODUCTION("experience_production", "XP", "経験値", Formatting.YELLOW, false),
    FORTUNE_FACTOR("fortune_factor", "FTN", "幸運係数", Formatting.DARK_PURPLE, false),
    NATURAL_ABSCISSION("natural_abscission", "NA", "自然落果", Formatting.BLUE, true),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val traitEffectKey = if (isLogScale) {
        object : TraitEffectKey<Double>() {
            override fun getValue(level: Int) = 1 - 0.95.pow(level.toDouble())
            override fun getDescription(value: Double) = text { getName() + (value * 100 formatAs "%+.0f%%")() }
            override fun plus(a: Double, b: Double) = 1.0 - (1.0 - a) * (1.0 - b)
            override fun getDefaultValue() = 0.0
        }
    } else {
        object : TraitEffectKey<Double>() {
            override fun getValue(level: Int) = 0.1 * level
            override fun getDescription(value: Double) = text { getName() + (value * 100 formatAs "%+.0f%%")() }
            override fun plus(a: Double, b: Double) = a + b
            override fun getDefaultValue() = 0.0
        }
    }
}


// TraitFactor

object TraitFactors {
    val ALWAYS = TraitFactor { _, _ -> 1.0 }
    val FLOOR_MOISTURE = TraitFactor { world, blockPos -> world.getMoisture(blockPos.down()) }
    val FLOOR_CRYSTAL_ERG = TraitFactor { world, blockPos -> world.getCrystalErg(blockPos.down()) }
    val FLOOR_HARDNESS = TraitFactor { world, blockPos ->
        val blockState = world.getBlockState(blockPos.down())
        if (!blockState.isIn(BlockTags.PICKAXE_MINEABLE)) return@TraitFactor 0.0
        val hardness = blockState.getHardness(world, blockPos.down())
        if (hardness < 0) return@TraitFactor 0.0
        hardness / 2.0 atMost 2.0
    }
    val LIGHT = TraitFactor { world, blockPos -> (world.getLightLevel(blockPos) - 8 atLeast 0) / 7.0 }
    val DARKNESS = TraitFactor { world, blockPos -> ((15 - world.getLightLevel(blockPos)) - 8 atLeast 0) / 7.0 }
    val LOW_TEMPERATURE = TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.LOW }
    val MEDIUM_TEMPERATURE = TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.MEDIUM }
    val HIGH_TEMPERATURE = TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.HIGH }
    val LOW_HUMIDITY = TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.LOW }
    val MEDIUM_HUMIDITY = TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.MEDIUM }
    val HIGH_HUMIDITY = TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.HIGH }
    val OUTDOOR = TraitCondition { world, blockPos -> blockPos.y >= world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).y }
}
