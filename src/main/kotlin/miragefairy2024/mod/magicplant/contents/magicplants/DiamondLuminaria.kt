package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.BiomeCards
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.get
import miragefairy2024.util.getOr
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.item.Items
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties as Properties
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider

object DiamondLuminariaConfiguration : SimpleMagicPlantConfiguration<DiamondLuminariaCard, DiamondLuminariaBlock>() {
    override val card get() = DiamondLuminariaCard

    override val blockPath = "diamond_luminaria"
    override val blockName = EnJa("Diamond Luminaria", "金剛石輝草ダイヤモンドルミナリア")
    override val itemPath = "diamond_luminaria_bulb"
    override val itemName = EnJa("Diamond Luminaria Bulb", "ダイヤモンドルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Fruits the crystallized carbon", "表土を飾る、凍てつく星。")
    override val classification = EnJa("Order Miragales, family Luminariaceae", "妖花目ルミナリア科")

    override fun createBlock() = DiamondLuminariaBlock(createCommonSettings().strength(0.2F).luminance { getLuminance(it.getOr(Properties.AGE_3) { 0 }) }.mapColor(MapColor.DIAMOND_BLUE).sounds(BlockSoundGroup.CROP))

    override val outlineShapes = listOf(
        createCuboidShape(4.0, 6.0),
        createCuboidShape(5.0, 13.0),
        createCuboidShape(7.0, 16.0),
        createCuboidShape(7.0, 16.0),
    )

    override val baseGrowth = 0.2
    override val baseFruitGeneration = 0.1

    override val drops = listOf(MaterialCard.LUMINITE.item, Items.DIAMOND)
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.LUMINITE.item.createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(Items.DIAMOND.createItemStack(count))

    override val family = MirageFairy2024.identifier("luminaria")
    override val possibleTraits = setOf(
        //TraitCard.ETHER_RESPIRATION.trait, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        //TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
        //TraitCard.AIR_ADAPTATION.trait, // 空間適応
        TraitCard.COLD_ADAPTATION.trait, // 低温適応
        TraitCard.WARM_ADAPTATION.trait, // 中温適応
        TraitCard.HOT_ADAPTATION.trait, // 高温適応
        TraitCard.ARID_ADAPTATION.trait, // 乾燥適応
        TraitCard.MESIC_ADAPTATION.trait, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait, // 葉面生成
        //TraitCard.RARE_PRODUCTION.trait, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait, // 経験値生成
        //TraitCard.FAIRY_BLESSING.trait, // 妖精の祝福
        //TraitCard.FOUR_LEAFED.trait, // 四つ葉
        //TraitCard.NODED_STEM.trait, // 節状の茎
        //TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        //TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        //TraitCard.DESERT_GEM.trait, // 砂漠の宝石
        TraitCard.HEATING_MECHANISM.trait, // 発熱機構
        //TraitCard.WATERLOGGING_TOLERANCE.trait, // 浸水耐性
        //TraitCard.ADVERSITY_FLOWER.trait, // 高嶺の花
        //TraitCard.FLESHY_LEAVES.trait, // 肉厚の葉
        //TraitCard.NATURAL_ABSCISSION.trait, // 自然落果
        //TraitCard.CARNIVOROUS_PLANT.trait, // 食虫植物
        //TraitCard.ETHER_PREDATION.trait, // エーテル捕食
        //TraitCard.PAVEMENT_FLOWERS.trait, // アスファルトに咲く花
        //TraitCard.PROSPERITY_OF_SPECIES.trait, // 種の繁栄
        //TraitCard.PHANTOM_FLOWER.trait, // 幻の花
        TraitCard.ETERNAL_TREASURE.trait, // 悠久の秘宝
        TraitCard.TREASURE_OF_XARPA.trait, // シャルパの秘宝
        //TraitCard.CROSSBREEDING.trait, // 交雑
        //TraitCard.PLANTS_WITH_SELF_AWARENESS.trait, // 自我を持つ植物
        //TraitCard.FLOWER_OF_THE_END.trait, // 終焉の花
    )

    val DIAMOND_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY = RegistryKeys.CONFIGURED_FEATURE with MirageFairy2024.identifier("diamond_luminaria_cluster")
    val DIAMOND_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("diamond_luminaria_cluster")

    context(ModContext)
    override fun init() {
        super.init()

        // 地形生成
        registerDynamicGeneration(DIAMOND_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY) {
            val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
            Feature.FLOWER with RandomPatchFeatureConfig(1, 0, 0, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
        }
        registerDynamicGeneration(DIAMOND_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY) {
            val placementModifiers = placementModifiers { per(32) + flower }
            RegistryKeys.CONFIGURED_FEATURE[DIAMOND_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY] with placementModifiers
        }
        DIAMOND_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +ConventionalBiomeTags.SNOWY }
        DIAMOND_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +ConventionalBiomeTags.ICY }
        DIAMOND_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +BiomeCards.FAIRY_FOREST.registryKey }

    }
}

object DiamondLuminariaCard : SimpleMagicPlantCard<DiamondLuminariaBlock>(DiamondLuminariaConfiguration)

class DiamondLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(DiamondLuminariaConfiguration, settings) {
    override fun getAgeProperty(): IntProperty = Properties.AGE_3
}

fun getLuminance(age: Int): Int {
    return when (age) {
        0 -> 2
        1 -> 4
        2 -> 8
        3 -> 15
        else -> 0
    }
}
