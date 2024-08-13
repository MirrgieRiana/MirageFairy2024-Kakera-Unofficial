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
    val enName: String,
    val jaName: String,
    enPoem: String,
    jaPoem: String,
    traitFactorCards: List<TraitFactorCard>,
    traitEffectKeyCards: List<TraitEffectKeyCard>,
) {
    companion object {
        val entries = mutableListOf<TraitCard>()
        operator fun TraitCard.not() = also { entries += this }

        val ETHER_RESPIRATION = !TraitCard(
            "ether_respiration", "Ether Respiration", "エーテル呼吸",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.NUTRITION),
        )
        val PHOTOSYNTHESIS = !TraitCard(
            "photosynthesis", "Photosynthesis", "光合成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.LIGHT), listOf(TraitEffectKeyCard.NUTRITION),
        )
        val PHAEOSYNTHESIS = !TraitCard(
            "phaeosynthesis", "Phaeosynthesis", "闇合成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.DARKNESS), listOf(TraitEffectKeyCard.NUTRITION),
        )
        val OSMOTIC_ABSORPTION = !TraitCard(
            "osmotic_absorption", "Osmotic Absorption", "浸透吸収",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.FLOOR_MOISTURE), listOf(TraitEffectKeyCard.NUTRITION),
        )
        val CRYSTAL_ABSORPTION = !TraitCard(
            "crystal_absorption", "Crystal Absorption", "鉱物吸収",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.FLOOR_CRYSTAL_ERG), listOf(TraitEffectKeyCard.NUTRITION),
        )

        val AIR_ADAPTATION = !TraitCard(
            "air_adaptation", "Air Adaptation", "空気適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val COLD_ADAPTATION = !TraitCard(
            "cold_adaptation", "Cold Adaptation", "寒冷適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.LOW_TEMPERATURE), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val WARM_ADAPTATION = !TraitCard(
            "warm_adaptation", "Warm Adaptation", "温暖適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.MEDIUM_TEMPERATURE), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val HOT_ADAPTATION = !TraitCard(
            "hot_adaptation", "Hot Adaptation", "熱帯適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.HIGH_TEMPERATURE), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val ARID_ADAPTATION = !TraitCard(
            "arid_adaptation", "Arid Adaptation", "乾燥適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val MESIC_ADAPTATION = !TraitCard(
            "mesic_adaptation", "Mesic Adaptation", "中湿適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.MEDIUM_HUMIDITY), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val HUMID_ADAPTATION = !TraitCard(
            "humid_adaptation", "Humid Adaptation", "湿潤適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.HIGH_HUMIDITY), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )

        val SEEDS_PRODUCTION = !TraitCard(
            "seeds_production", "Seeds Production", "種子生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.SEEDS_PRODUCTION),
        )
        val FRUITS_PRODUCTION = !TraitCard(
            "fruits_production", "Fruits Production", "果実生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION),
        )
        val LEAVES_PRODUCTION = !TraitCard(
            "leaves_production", "Leaves Production", "葉面生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.LEAVES_PRODUCTION),
        )
        val RARE_PRODUCTION = !TraitCard(
            "rare_production", "Rare Production", "希少品生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.RARE_PRODUCTION),
        )
        val EXPERIENCE_PRODUCTION = !TraitCard(
            "experience_production", "Xp Production", "経験値生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.EXPERIENCE_PRODUCTION),
        )

        val FAIRY_BLESSING = !TraitCard(
            "fairy_blessing", "Fairy Blessing", "妖精の祝福",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR),
        )

        val FOUR_LEAFED = !TraitCard(
            "four_leafed", "Four-leafed", "四つ葉",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR),
        )
        val NODED_STEM = !TraitCard(
            "noded_stem", "Noded Stem", "節状の茎",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.GROWTH_BOOST),
        )
        val FRUIT_OF_KNOWLEDGE = !TraitCard(
            "fruit_of_knowledge", "Fruit of Knowledge", "知識の果実",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.EXPERIENCE_PRODUCTION),
        )
        val GOLDEN_APPLE = !TraitCard(
            "golden_apple", "Golden Apple", "金のリンゴ",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR),
        )
        val SPINY_LEAVES = !TraitCard(
            "spiny_leaves", "Spiny Leaves", "棘状の葉",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val DESERT_GEM = !TraitCard(
            "desert_gem", "Desert Gem", "砂漠の宝石",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.PRODUCTION_BOOST),
        )
        val HEATING_MECHANISM = !TraitCard(
            "heating_mechanism", "Heating Mechanism", "発熱機構",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.LOW_TEMPERATURE), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val WATERLOGGING_TOLERANCE = !TraitCard(
            "waterlogging_tolerance", "Waterlogging Tolerance", "浸水耐性",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.HIGH_HUMIDITY), listOf(TraitEffectKeyCard.ENVIRONMENT),
        )
        val ADVERSITY_FLOWER = !TraitCard(
            "adversity_flower", "Adversity Flower", "高嶺の花",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION),
        )
        val FLESHY_LEAVES = !TraitCard(
            "fleshy_leaves", "Fleshy Leaves", "肉厚の葉",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.LEAVES_PRODUCTION),
        )
        val NATURAL_ABSCISSION = !TraitCard(
            "natural_abscission", "Natural Abscission", "自然落果",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.NATURAL_ABSCISSION),
        )
        val CARNIVOROUS_PLANT = !TraitCard(
            "carnivorous_plant", "Carnivorous Plant", "食虫植物",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.OUTDOOR), listOf(TraitEffectKeyCard.NUTRITION),
        )
        val ETHER_PREDATION = !TraitCard(
            "ether_predation", "Ether Predation", "エーテル捕食",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.NUTRITION),
        )
        val PAVEMENT_FLOWERS = !TraitCard(
            "pavement_flowers", "Pavement Flowers", "アスファルトに咲く花",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitFactorCard.FLOOR_HARDNESS), listOf(TraitEffectKeyCard.NUTRITION),
        )
        val PROSPERITY_OF_SPECIES = !TraitCard(
            "prosperity_of_species", "Prosperity of Species", "種の繁栄",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.SEEDS_PRODUCTION),
        )
    }

    init {
        check(traitEffectKeyCards.isNotEmpty())
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    val trait: Trait = CompoundTrait(traitFactorCards.map { it.traitFactor }, traitEffectKeyCards)

    private class CompoundTrait(private val factors: List<TraitFactor>, private val traitEffectKeyCards: List<TraitEffectKeyCard>) : Trait(traitEffectKeyCards.first().color) {
        override val primaryEffect = traitEffectKeyCards.first().traitEffectKey
        override fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects? {
            val factor = factors.map { it.getFactor(world, blockPos) }.fold(1.0) { a, b -> a * b }
            return if (factor != 0.0) {
                val traitEffects = MutableTraitEffects()
                traitEffectKeyCards.forEach {
                    traitEffects[it.traitEffectKey] = it.traitEffectKey.getValue(level) * factor
                }
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
