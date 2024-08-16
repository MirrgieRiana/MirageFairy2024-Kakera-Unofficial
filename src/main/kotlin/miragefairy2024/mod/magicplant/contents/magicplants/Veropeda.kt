package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.nether
import miragefairy2024.util.netherFlower
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.MapColor
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.random.Random
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.stateprovider.BlockStateProvider

object VeropedaSettings : SimpleMagicPlantSettings<VeropedaCard, VeropedaBlock>() {
    override val card get() = VeropedaCard

    override val blockPath = "veropeda"
    override val blockEnName = "Veropeda"
    override val blockJaName = "呪草ヴェロペダ"
    override val itemPath = "veropeda_bulb"
    override val itemEnName = "Veropeda Bulb"
    override val itemJaName = "ヴェロペダの球根"
    override val tier = 1
    override val enPoem = "Contains strong acids made from insects"
    override val jaPoem = "毒を喰らい、毒と化す。"
    override val enClassification = "Order Miragales, family Veropedaceae"
    override val jaClassification = "妖花目ヴェロペダ科"

    override fun createBlock() = VeropedaBlock(createCommonSettings().breakInstantly().mapColor(MapColor.DARK_RED).sounds(BlockSoundGroup.CROP))

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(4.0, 7.0),
        createCuboidShape(7.0, 9.0),
        createCuboidShape(7.0, 16.0),
    )

    override val drops = listOf(MaterialCard.VEROPEDA_BERRIES.item, MaterialCard.VEROPEDA_LEAF.item)
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.VEROPEDA_BERRIES.item.createItemStack(count))
    override fun getLeafDrops(count: Int, random: Random) = listOf(MaterialCard.VEROPEDA_LEAF.item.createItemStack(count))

    override val family = MirageFairy2024.identifier("veropeda")
    override val possibleTraits = setOf(
        //TraitCard.ETHER_RESPIRATION.trait, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 浸透吸収
        //TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
        //TraitCard.AIR_ADAPTATION.trait, // 空気適応
        TraitCard.COLD_ADAPTATION.trait, // 寒冷適応
        TraitCard.WARM_ADAPTATION.trait, // 温暖適応
        TraitCard.HOT_ADAPTATION.trait, // 熱帯適応
        TraitCard.ARID_ADAPTATION.trait, // 乾燥適応
        TraitCard.MESIC_ADAPTATION.trait, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait, // 経験値生成
        //TraitCard.FAIRY_BLESSING.trait, // 妖精の祝福
        //TraitCard.FOUR_LEAFED.trait, // 四つ葉
        //TraitCard.NODED_STEM.trait, // 節状の茎
        //TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 知識の果実
        TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        TraitCard.SPINY_LEAVES.trait, // 棘状の葉
        TraitCard.DESERT_GEM.trait, // 砂漠の宝石
        //TraitCard.HEATING_MECHANISM.trait, // 発熱機構
        //TraitCard.WATERLOGGING_TOLERANCE.trait, // 浸水耐性
        TraitCard.ADVERSITY_FLOWER.trait, // 高嶺の花
        TraitCard.FLESHY_LEAVES.trait, // 肉厚の葉
        TraitCard.NATURAL_ABSCISSION.trait, // 自然落果
        TraitCard.CARNIVOROUS_PLANT.trait, // 食虫植物
        //TraitCard.ETHER_PREDATION.trait, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait, // アスファルトに咲く花
        TraitCard.PROSPERITY_OF_SPECIES.trait, // 種の繁栄
        //TraitCard.PHANTOM_FLOWER.trait, // 幻の花
        //TraitCard.ETERNAL_TREASURE.trait, // 悠久の秘宝
        //TraitCard.TREASURE_OF_XARPA.trait, // シャルパの秘宝
        TraitCard.CROSSBREEDING.trait, // 交雑
    )

    val VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY = RegistryKeys.CONFIGURED_FEATURE with MirageFairy2024.identifier("veropeda_cluster")
    val LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY = RegistryKeys.CONFIGURED_FEATURE with MirageFairy2024.identifier("large_veropeda_cluster")
    val VEROPEDA_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("veropeda_cluster")
    val NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("nether_veropeda_cluster")

    context(ModContext)
    override fun init() {
        super.init()

        // 地形生成
        run {

            // 小さな塊
            registerDynamicGeneration(VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 大きな塊
            registerDynamicGeneration(LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(40, 8, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 地上
            registerDynamicGeneration(VEROPEDA_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(16) + flower }
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // ネザー
            registerDynamicGeneration(NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(8) + netherFlower }
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            VEROPEDA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +ConventionalBiomeTags.CLIMATE_DRY } // 地上用クラスタ
            NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { nether } // ネザー用クラスタ

        }

    }
}

object VeropedaCard : SimpleMagicPlantCard<VeropedaBlock>(VeropedaSettings)

class VeropedaBlock(settings: Settings) : SimpleMagicPlantBlock(VeropedaSettings, settings) {
    override fun getAgeProperty(): IntProperty = Properties.AGE_3
}
