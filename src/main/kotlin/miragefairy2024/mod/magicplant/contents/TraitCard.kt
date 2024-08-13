package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.TraitFactor
import miragefairy2024.mod.magicplant.enJa
import miragefairy2024.mod.magicplant.traitRegistry
import miragefairy2024.util.register
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TraitCard(
    path: String,
    sortKey: String,
    val enName: String,
    val jaName: String,
    enPoem: String,
    jaPoem: String,
    traitFactorCard: TraitFactorCard,
    traitEffectKeyCard: TraitEffectKeyCard,
) {
    companion object {
        val entries = mutableListOf<TraitCard>()
        operator fun TraitCard.not() = also { entries += this }

        val ETHER_RESPIRATION = !TraitCard(
            "ether_respiration", "0nutrition", "Ether Respiration", "エーテル呼吸",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.NUTRITION,
        )
        val PHOTOSYNTHESIS = !TraitCard(
            "photosynthesis", "0nutrition", "Photosynthesis", "光合成",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.LIGHT, TraitEffectKeyCard.NUTRITION,
        )
        val PHAEOSYNTHESIS = !TraitCard(
            "phaeosynthesis", "0nutrition", "Phaeosynthesis", "闇合成",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.DARKNESS, TraitEffectKeyCard.NUTRITION,
        )
        val OSMOTIC_ABSORPTION = !TraitCard(
            "osmotic_absorption", "0nutrition", "Osmotic Absorption", "浸透吸収",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.FLOOR_MOISTURE, TraitEffectKeyCard.NUTRITION,
        )
        val CRYSTAL_ABSORPTION = !TraitCard(
            "crystal_absorption", "0nutrition", "Crystal Absorption", "鉱物吸収",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.FLOOR_CRYSTAL_ERG, TraitEffectKeyCard.NUTRITION,
        )

        val AIR_ADAPTATION = !TraitCard(
            "air_adaptation", "1atmosphere", "Air Adaptation", "空気適応",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.ENVIRONMENT,
        )
        val COLD_ADAPTATION = !TraitCard(
            "cold_adaptation", "3biome", "Cold Adaptation", "寒冷適応",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT,
        )
        val WARM_ADAPTATION = !TraitCard(
            "warm_adaptation", "3biome", "Warm Adaptation", "温暖適応",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.MEDIUM_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT,
        )
        val HOT_ADAPTATION = !TraitCard(
            "hot_adaptation", "3biome", "Hot Adaptation", "熱帯適応",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.HIGH_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT,
        )
        val ARID_ADAPTATION = !TraitCard(
            "arid_adaptation", "3biome", "Arid Adaptation", "乾燥適応",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT,
        )
        val MESIC_ADAPTATION = !TraitCard(
            "mesic_adaptation", "3biome", "Mesic Adaptation", "中湿適応",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.MEDIUM_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT,
        )
        val HUMID_ADAPTATION = !TraitCard(
            "humid_adaptation", "3biome", "Humid Adaptation", "湿潤適応",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT,
        )

        val SEEDS_PRODUCTION = !TraitCard(
            "seeds_production", "4production", "Seeds Production", "種子生成",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION,
        )
        val FRUITS_PRODUCTION = !TraitCard(
            "fruits_production", "4production", "Fruits Production", "果実生成",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION,
        )
        val LEAVES_PRODUCTION = !TraitCard(
            "leaves_production", "4production", "Leaves Production", "葉面生成",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.LEAVES_PRODUCTION,
        )
        val RARE_PRODUCTION = !TraitCard(
            "rare_production", "4production", "Rare Production", "希少品生成",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.RARE_PRODUCTION,
        )
        val EXPERIENCE_PRODUCTION = !TraitCard(
            "experience_production", "4production", "Xp Production", "経験値生成",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION,
        )

        val FAIRY_BLESSING = !TraitCard(
            "fairy_blessing", "5skill", "Fairy Blessing", "妖精の祝福",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR,
        )

        val FOUR_LEAFED = !TraitCard(
            "four_leafed", "6part", "Four-leafed", "四つ葉",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR,
        )
        val NODED_STEM = !TraitCard(
            "noded_stem", "6part", "Noded Stem", "節状の茎",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.GROWTH_BOOST,
        )
        val FRUIT_OF_KNOWLEDGE = !TraitCard(
            "fruit_of_knowledge", "6part", "Fruit of Knowledge", "知識の果実",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.EXPERIENCE_PRODUCTION,
        )
        val GOLDEN_APPLE = !TraitCard(
            "golden_apple", "6part", "Golden Apple", "金のリンゴ",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.FORTUNE_FACTOR,
        )
        val SPINY_LEAVES = !TraitCard(
            "spiny_leaves", "6part", "Spiny Leaves", "棘状の葉",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT,
        )
        val DESERT_GEM = !TraitCard(
            "desert_gem", "6part", "Desert Gem", "砂漠の宝石",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.PRODUCTION_BOOST,
        )
        val HEATING_MECHANISM = !TraitCard(
            "heating_mechanism", "6part", "Heating Mechanism", "発熱機構",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.LOW_TEMPERATURE, TraitEffectKeyCard.ENVIRONMENT,
        )
        val WATERLOGGING_TOLERANCE = !TraitCard(
            "waterlogging_tolerance", "6part", "Waterlogging Tolerance", "浸水耐性",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.HIGH_HUMIDITY, TraitEffectKeyCard.ENVIRONMENT,
        )
        val ADVERSITY_FLOWER = !TraitCard(
            "adversity_flower", "6part", "Adversity Flower", "高嶺の花",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.FRUITS_PRODUCTION,
        )
        val FLESHY_LEAVES = !TraitCard(
            "fleshy_leaves", "6part", "Fleshy Leaves", "肉厚の葉",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.LOW_HUMIDITY, TraitEffectKeyCard.LEAVES_PRODUCTION,
        )
        val NATURAL_ABSCISSION = !TraitCard(
            "natural_abscission", "6part", "Natural Abscission", "自然落果",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.NATURAL_ABSCISSION,
        )
        val CARNIVOROUS_PLANT = !TraitCard(
            "carnivorous_plant", "6part", "Carnivorous Plant", "食虫植物",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.OUTDOOR, TraitEffectKeyCard.NUTRITION,
        )
        val ETHER_PREDATION = !TraitCard(
            "ether_predation", "6part", "Ether Predation", "エーテル捕食",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.NUTRITION,
        )
        val PAVEMENT_FLOWERS = !TraitCard(
            "pavement_flowers", "6part", "Pavement Flowers", "アスファルトに咲く花",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.FLOOR_HARDNESS, TraitEffectKeyCard.NUTRITION,
        )
        val PROSPERITY_OF_SPECIES = !TraitCard(
            "prosperity_of_species", "6part", "Prosperity of Species", "種の繁栄",
            "TODO", // TODO
            "TODO", // TODO
            TraitFactorCard.ALWAYS, TraitEffectKeyCard.SEEDS_PRODUCTION,
        )
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    val trait: Trait = CompoundTrait(sortKey, traitFactorCard.traitFactor, traitEffectKeyCard)

    private class CompoundTrait(sortKey: String, private val factor: TraitFactor, private val traitEffectKeyCard: TraitEffectKeyCard) : Trait(traitEffectKeyCard.color, sortKey) {
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

}

context(ModContext)
fun initTraitCard() {
    TraitCard.entries.forEach { card ->
        card.trait.register(traitRegistry, card.identifier)
        card.trait.enJa(card.enName, card.jaName)
    }
}
