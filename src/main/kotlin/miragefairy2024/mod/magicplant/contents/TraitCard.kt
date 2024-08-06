package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.TraitFactor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

enum class TraitCard(
    path: String,
    sortKey: String,
    val enName: String,
    val jaName: String,
    traitFactorCard: TraitFactorCard,
    traitEffectKeyCard: TraitEffectKeyCard,
) {
    ETHER_RESPIRATION("ether_respiration", "0nutrition", "Ether Respiration", "エーテル呼吸", TraitFactorCard.ALWAYS, TraitEffectKeyCard.NUTRITION),
    PHOTOSYNTHESIS("photosynthesis", "0nutrition", "Photosynthesis", "光合成", TraitFactorCard.LIGHT, TraitEffectKeyCard.NUTRITION),
    PHAEOSYNTHESIS("phaeosynthesis", "0nutrition", "Phaeosynthesis", "闇合成", TraitFactorCard.DARKNESS, TraitEffectKeyCard.NUTRITION),
    OSMOTIC_ABSORPTION("osmotic_absorption", "0nutrition", "Osmotic Absorption", "浸透吸収", TraitFactorCard.FLOOR_MOISTURE, TraitEffectKeyCard.NUTRITION),
    CRYSTAL_ABSORPTION("crystal_absorption", "0nutrition", "Crystal Absorption", "鉱物吸収", TraitFactorCard.FLOOR_CRYSTAL_ERG, TraitEffectKeyCard.NUTRITION),

    AIR_ADAPTATION("air_adaptation", "1atmosphere", "Air Adaptation", "空気適応", TraitFactorCard.ALWAYS, TraitEffectKeyCard.ENVIRONMENT),
    COLD_ADAPTATION("cold_adaptation", "3biome", "Cold Adaptation", "寒冷適応", TraitFactorCard.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    WARM_ADAPTATION("warm_adaptation", "3biome", "Warm Adaptation", "温暖適応", TraitFactorCard.MEDIUM_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    HOT_ADAPTATION("hot_adaptation", "3biome", "Hot Adaptation", "熱帯適応", TraitFactorCard.HIGH_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    ARID_ADAPTATION("arid_adaptation", "3biome", "Arid Adaptation", "乾燥適応", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    MESIC_ADAPTATION("mesic_adaptation", "3biome", "Mesic Adaptation", "中湿適応", TraitFactorCard.MEDIUM_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    HUMID_ADAPTATION("humid_adaptation", "3biome", "Humid Adaptation", "湿潤適応", TraitFactorCard.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),

    SEEDS_PRODUCTION("seeds_production", "4production", "Seeds Production", "種子生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION),
    FRUITS_PRODUCTION("fruits_production", "4production", "Fruits Production", "果実生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION),
    LEAVES_PRODUCTION("leaves_production", "4production", "Leaves Production", "葉面生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.LEAVES_PRODUCTION),
    RARE_PRODUCTION("rare_production", "4production", "Rare Production", "希少品生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.RARE_PRODUCTION),
    EXPERIENCE_PRODUCTION("experience_production", "4production", "Xp Production", "経験値生成", TraitFactorCard.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION),

    FAIRY_BLESSING("fairy_blessing", "5skill", "Fairy Blessing", "妖精の祝福", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR),

    FOUR_LEAFED("four_leafed", "6part", "Four-leafed", "四つ葉", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR),
    NODED_STEM("noded_stem", "6part", "Noded Stem", "節状の茎", TraitFactorCard.ALWAYS, TraitEffectKeyCard.GROWTH_BOOST),
    FRUIT_OF_KNOWLEDGE("fruit_of_knowledge", "6part", "Fruit of Knowledge", "知識の果実", TraitFactorCard.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION),
    GOLDEN_APPLE("golden_apple", "6part", "Golden Apple", "金のリンゴ", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR),
    SPINY_LEAVES("spiny_leaves", "6part", "Spiny Leaves", "棘状の葉", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    DESERT_GEM("desert_gem", "6part", "Desert Gem", "砂漠の宝石", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.PRODUCTION_BOOST),
    HEATING_MECHANISM("heating_mechanism", "6part", "Heating Mechanism", "発熱機構", TraitFactorCard.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT),
    WATERLOGGING_TOLERANCE("waterlogging_tolerance", "6part", "Waterlogging Tolerance", "浸水耐性", TraitFactorCard.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT),
    ADVERSITY_FLOWER("adversity_flower", "6part", "Adversity Flower", "高嶺の花", TraitFactorCard.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION),
    FLESHY_LEAVES("fleshy_leaves", "6part", "Fleshy Leaves", "肉厚の葉", TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.LEAVES_PRODUCTION),
    NATURAL_ABSCISSION("natural_abscission", "6part", "Natural Abscission", "自然落果", TraitFactorCard.ALWAYS, TraitEffectKeyCard.NATURAL_ABSCISSION),
    CARNIVOROUS_PLANT("carnivorous_plant", "6part", "Carnivorous Plant", "食虫植物", TraitFactorCard.OUTDOOR, TraitEffectKeyCard.NUTRITION),
    ETHER_PREDATION("ether_predation", "6part", "Ether Predation", "エーテル捕食", TraitFactorCard.ALWAYS, TraitEffectKeyCard.NUTRITION),
    PAVEMENT_FLOWERS("pavement_flowers", "6part", "Pavement Flowers", "アスファルトに咲く花", TraitFactorCard.FLOOR_HARDNESS, TraitEffectKeyCard.NUTRITION),
    PROSPERITY_OF_SPECIES("prosperity_of_species", "6part", "Prosperity of Species", "種の繁栄", TraitFactorCard.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val trait = CompoundTrait(sortKey, traitFactorCard.traitFactor, traitEffectKeyCard)
}

class CompoundTrait(sortKey: String, private val factor: TraitFactor, private val traitEffectKeyCard: TraitEffectKeyCard) : Trait(traitEffectKeyCard.color, sortKey) {
    override fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects? {
        val factor = factor.getFactor(world, blockPos)
        return if (factor != 0.0) {
            val traitEffects = MutableTraitEffects()
            traitEffects[traitEffectKeyCard.traitEffectKey] = traitEffectKeyCard.traitEffectKey.getValue(level) * factor
            traitEffects
        } else {
            null
        }
    }
}
