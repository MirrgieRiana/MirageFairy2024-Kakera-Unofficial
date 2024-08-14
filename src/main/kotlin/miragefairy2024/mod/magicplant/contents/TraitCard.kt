package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.TraitSpawnCondition
import miragefairy2024.mod.magicplant.TraitSpawnConditionScope
import miragefairy2024.mod.magicplant.TraitSpawnRarity
import miragefairy2024.mod.magicplant.TraitSpawnSpec
import miragefairy2024.mod.magicplant.anywhere
import miragefairy2024.mod.magicplant.enJa
import miragefairy2024.mod.magicplant.traitRegistry
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TraitCard(
    path: String,
    val enName: String,
    val jaName: String,
    enPoem: String,
    jaPoem: String,
    private val traitConditionCards: List<TraitConditionCard>,
    private val traitEffectKeyCardStacks: List<Pair<TraitEffectKeyCard, Double>>,
    private val spawnSpecConfigurator: MutableList<TraitSpawnSpec>.() -> Unit,
) {
    companion object {
        val entries = mutableListOf<TraitCard>()
        operator fun TraitCard.not() = also { entries += this }

        val ETHER_RESPIRATION = !TraitCard(
            "ether_respiration", "Ether Respiration", "エーテル呼吸",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }
        val PHOTOSYNTHESIS = !TraitCard(
            "photosynthesis", "Photosynthesis", "光合成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.LIGHT), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }
        val PHAEOSYNTHESIS = !TraitCard(
            "phaeosynthesis", "Phaeosynthesis", "闇合成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.DARKNESS), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }
        val OSMOTIC_ABSORPTION = !TraitCard(
            "osmotic_absorption", "Osmotic Absorption", "浸透吸収",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.FLOOR_MOISTURE), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }
        val CRYSTAL_ABSORPTION = !TraitCard(
            "crystal_absorption", "Crystal Absorption", "鉱物吸収",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.FLOOR_CRYSTAL_ERG), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }

        val AIR_ADAPTATION = !TraitCard(
            "air_adaptation", "Air Adaptation", "空気適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.TEMPERATURE to 0.05, TraitEffectKeyCard.HUMIDITY to 0.05),
        ) {

        }
        val COLD_ADAPTATION = !TraitCard(
            "cold_adaptation", "Cold Adaptation", "寒冷適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.LOW_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.1),
        ) {

        }
        val WARM_ADAPTATION = !TraitCard(
            "warm_adaptation", "Warm Adaptation", "温暖適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.MEDIUM_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.1),
        ) {

        }
        val HOT_ADAPTATION = !TraitCard(
            "hot_adaptation", "Hot Adaptation", "熱帯適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.HIGH_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.1),
        ) {

        }
        val ARID_ADAPTATION = !TraitCard(
            "arid_adaptation", "Arid Adaptation", "乾燥適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {

        }
        val MESIC_ADAPTATION = !TraitCard(
            "mesic_adaptation", "Mesic Adaptation", "中湿適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.MEDIUM_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {

        }
        val HUMID_ADAPTATION = !TraitCard(
            "humid_adaptation", "Humid Adaptation", "湿潤適応",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.HIGH_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {

        }

        val SEEDS_PRODUCTION = !TraitCard(
            "seeds_production", "Seeds Production", "種子生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.SEEDS_PRODUCTION to 0.1),
        ) {

        }
        val FRUITS_PRODUCTION = !TraitCard(
            "fruits_production", "Fruits Production", "果実生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION to 0.1),
        ) {

        }
        val LEAVES_PRODUCTION = !TraitCard(
            "leaves_production", "Leaves Production", "葉面生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.LEAVES_PRODUCTION to 0.1),
        ) {

        }
        val RARE_PRODUCTION = !TraitCard(
            "rare_production", "Rare Production", "希少品生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.RARE_PRODUCTION to 0.003),
        ) {

        }
        val EXPERIENCE_PRODUCTION = !TraitCard(
            "experience_production", "Xp Production", "経験値生成",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.EXPERIENCE_PRODUCTION to 0.1),
        ) {

        }

        val FAIRY_BLESSING = !TraitCard(
            "fairy_blessing", "Fairy Blessing", "妖精の祝福",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {

        }

        val FOUR_LEAFED = !TraitCard(
            "four_leafed", "Four-leafed", "四つ葉",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {

        }
        val NODED_STEM = !TraitCard(
            "noded_stem", "Noded Stem", "節状の茎",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.GROWTH_BOOST to 0.1),
        ) {

        }
        val FRUIT_OF_KNOWLEDGE = !TraitCard(
            "fruit_of_knowledge", "Fruit of Knowledge", "知識の果実",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.EXPERIENCE_PRODUCTION to 0.1),
        ) {

        }
        val GOLDEN_APPLE = !TraitCard(
            "golden_apple", "Golden Apple", "金のリンゴ",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {

        }
        val SPINY_LEAVES = !TraitCard(
            "spiny_leaves", "Spiny Leaves", "棘状の葉",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {

        }
        val DESERT_GEM = !TraitCard(
            "desert_gem", "Desert Gem", "砂漠の宝石",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.PRODUCTION_BOOST to 0.1),
        ) {

        }
        val HEATING_MECHANISM = !TraitCard(
            "heating_mechanism", "Heating Mechanism", "発熱機構",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.LOW_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.1),
        ) {

        }
        val WATERLOGGING_TOLERANCE = !TraitCard(
            "waterlogging_tolerance", "Waterlogging Tolerance", "浸水耐性",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.HIGH_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {

        }
        val ADVERSITY_FLOWER = !TraitCard(
            "adversity_flower", "Adversity Flower", "高嶺の花",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION to 0.1),
        ) {

        }
        val FLESHY_LEAVES = !TraitCard(
            "fleshy_leaves", "Fleshy Leaves", "肉厚の葉",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.LEAVES_PRODUCTION to 0.1),
        ) {

        }
        val NATURAL_ABSCISSION = !TraitCard(
            "natural_abscission", "Natural Abscission", "自然落果",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.NATURAL_ABSCISSION to 0.1),
        ) {

        }
        val CARNIVOROUS_PLANT = !TraitCard(
            "carnivorous_plant", "Carnivorous Plant", "食虫植物",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.OUTDOOR), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }
        val ETHER_PREDATION = !TraitCard(
            "ether_predation", "Ether Predation", "エーテル捕食",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }
        val PAVEMENT_FLOWERS = !TraitCard(
            "pavement_flowers", "Pavement Flowers", "アスファルトに咲く花",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.FLOOR_HARDNESS), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {

        }
        val PROSPERITY_OF_SPECIES = !TraitCard(
            "prosperity_of_species", "Prosperity of Species", "種の繁栄",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.SEEDS_PRODUCTION to 0.1),
        ) {

        }
        val PHANTOM_FLOWER = !TraitCard(
            "phantom_flower", "Phantom Flower", "幻の花",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {

        }
        val ETERNAL_TREASURE = !TraitCard(
            "eternal_treasure", "Eternal Treasure", "悠久の秘宝",
            "TODO", // TODO
            "TODO", // TODO
            listOf(TraitConditionCard.NATURAL), listOf(TraitEffectKeyCard.RARE_PRODUCTION to 0.1),
        ) {

        }
        val TREASURE_OF_XARPA = !TraitCard(
            "treasure_of_xarpa", "Treasure of Xarpa", "シャルパの秘宝",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.RARE_PRODUCTION to 0.001),
        ) {

        }
        val CROSSBREEDING = !TraitCard(
            "crossbreeding", "Crossbreeding", "交雑",
            "TODO", // TODO
            "TODO", // TODO
            listOf(), listOf(TraitEffectKeyCard.CROSSBREEDING to 0.1),
        ) {

        }
    }

    init {
        check(traitEffectKeyCardStacks.isNotEmpty())
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    val poemTranslation = Translation({ identifier.toTranslationKey("${MirageFairy2024.modId}.trait", "poem") }, enPoem, jaPoem)
    val trait: Trait = object : Trait(traitEffectKeyCardStacks.first().first.traitEffectKey.style, text { poemTranslation() }) {
        override val spawnSpecs = mutableListOf<TraitSpawnSpec>().also { spawnSpecConfigurator(it) }

        override val conditions = traitConditionCards.map { it.traitCondition }
        override val primaryEffect = traitEffectKeyCardStacks.first().first.traitEffectKey
        override val effectStacks = traitEffectKeyCardStacks.map { Pair(it.first.traitEffectKey, it.second) }

        override fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects? {
            val factor = traitConditionCards.map { it.traitCondition.getFactor(world, blockPos) }.fold(1.0) { a, b -> a * b }
            return if (factor != 0.0) {
                val traitEffects = MutableTraitEffects()
                traitEffectKeyCardStacks.forEach {
                    traitEffects[it.first.traitEffectKey] = it.first.traitEffectKey.getValue(it.second * level) * factor
                }
                traitEffects
            } else {
                null
            }
        }
    }

}

private fun MutableList<TraitSpawnSpec>.register(binary: String, rarity: TraitSpawnRarity, conditionGetter: context(TraitSpawnConditionScope)() -> TraitSpawnCondition = { anywhere }) {
    this += TraitSpawnSpec(conditionGetter(TraitSpawnConditionScope), rarity, binary.toInt(2))
}


context(ModContext)
fun initTraitCard() {
    TraitCard.entries.forEach { card ->
        card.poemTranslation.enJa()
        card.trait.register(traitRegistry, card.identifier)
        card.trait.enJa(card.enName, card.jaName)
    }
}
