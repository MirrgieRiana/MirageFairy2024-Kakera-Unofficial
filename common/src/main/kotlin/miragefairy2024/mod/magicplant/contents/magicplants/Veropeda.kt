package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.get
import miragefairy2024.util.nether
import miragefairy2024.util.netherFlower
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig

object VeropedaConfiguration : SimpleMagicPlantConfiguration<VeropedaCard, VeropedaBlock>() {
    override val card get() = VeropedaCard

    override val blockPath = "veropeda"
    override val blockName = EnJa("Veropeda", "呪草ヴェロペダ")
    override val itemPath = "veropeda_bulb"
    override val itemName = EnJa("Veropeda Bulb", "ヴェロペダの球根")
    override val tier = 1
    override val poem = EnJa("Contains strong acids made from insects", "毒を喰らい、毒と化す。")
    override val classification = EnJa("Order Miragales, family Veropedaceae", "妖花目ヴェロペダ科")

    override fun createBlock() = VeropedaBlock(createCommonSettings().breakInstantly().mapColor(MapColor.NETHER).sound(BlockSoundGroup.CROP))

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
        TraitCard.OSMOTIC_ABSORPTION.trait, // 養分吸収
        //TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
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
        TraitCard.RARE_PRODUCTION.trait, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait, // 経験値生成
        //TraitCard.FAIRY_BLESSING.trait, // 妖精の祝福
        TraitCard.FOUR_LEAFED.trait, // 四つ葉
        //TraitCard.NODED_STEM.trait, // 節状の茎
        TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        //TraitCard.DESERT_GEM.trait, // 砂漠の宝石
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
        //TraitCard.PLANTS_WITH_SELF_AWARENESS.trait, // 自我を持つ植物
        //TraitCard.FLOWER_OF_THE_END.trait, // 終焉の花
    )

    val VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("veropeda_cluster")
    val LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("large_veropeda_cluster")
    val VEROPEDA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("veropeda_cluster")
    val NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("nether_veropeda_cluster")

    context(ModContext)
    override fun init() {
        super.init()

        BuiltInRegistries.BLOCK_TYPE.register(MirageFairy2024.identifier("veropeda")) { VeropedaBlock.CODEC }

        // 地形生成
        run {

            // 小さな塊
            registerDynamicGeneration(VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.simple(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 大きな塊
            registerDynamicGeneration(LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.simple(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(40, 8, 3, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 地上
            registerDynamicGeneration(VEROPEDA_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(16) + flower }
                Registries.CONFIGURED_FEATURE[VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY] with placementModifiers
            }

            // ネザー
            registerDynamicGeneration(NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(8) + netherFlower }
                Registries.CONFIGURED_FEATURE[LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY] with placementModifiers
            }

            VEROPEDA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { +ConventionalBiomeTags.IS_DRY } // 地上用クラスタ
            NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { nether } // ネザー用クラスタ

        }

    }
}

object VeropedaCard : SimpleMagicPlantCard<VeropedaBlock>(VeropedaConfiguration)

class VeropedaBlock(settings: Properties) : SimpleMagicPlantBlock(VeropedaConfiguration, settings) {
    companion object {
        val CODEC: MapCodec<VeropedaBlock> = simpleCodec(::VeropedaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}
