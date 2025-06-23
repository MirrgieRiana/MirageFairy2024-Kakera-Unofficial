package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.nether
import miragefairy2024.util.netherFlower
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig

abstract class AbstractVeropedaCard<B : SimpleMagicPlantBlock> : SimpleMagicPlantCard<B>() {
    override val classification = EnJa("Order Miragales, family Veropedaceae", "妖花目ヴェロペダ科")

    override val ageProperty: IntegerProperty = BlockStateProperties.AGE_3

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(4.0, 7.0),
        createCuboidShape(7.0, 9.0),
        createCuboidShape(7.0, 16.0),
    )

    override val family = MirageFairy2024.identifier("veropeda")
    override val possibleTraits = setOf(
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 養分吸収
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
        TraitCard.FOUR_LEAFED.trait, // 四つ葉
        TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        TraitCard.ADVERSITY_FLOWER.trait, // 高嶺の花
        TraitCard.FLESHY_LEAVES.trait, // 肉厚の葉
        TraitCard.NATURAL_ABSCISSION.trait, // 自然落果
        TraitCard.CARNIVOROUS_PLANT.trait, // 食虫植物
        TraitCard.PAVEMENT_FLOWERS.trait, // アスファルトに咲く花
        TraitCard.PROSPERITY_OF_SPECIES.trait, // 種の繁栄
        TraitCard.CROSSBREEDING.trait, // 交雑
    )
}

object VeropedaCard : AbstractVeropedaCard<VeropedaBlock>() {
    override fun getBlockPath() = "veropeda"
    override val blockName = EnJa("Veropeda", "呪草ヴェロペダ")
    override fun getItemPath() = "veropeda_bulb"
    override val itemName = EnJa("Veropeda Bulb", "呪草ヴェロペダの球根")
    override val tier = 1
    override val poem = EnJa("Contains strong acids made from insects", "毒を喰らい、毒と化す。")

    override val blockCodec = VeropedaBlock.CODEC
    override fun createBlock() = VeropedaBlock(createCommonSettings().breakInstantly().mapColor(MapColor.NETHER).sound(BlockSoundGroup.CROP))

    override val drops = listOf(MaterialCard.VEROPEDA_BERRIES.item, MaterialCard.VEROPEDA_LEAF.item)
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.VEROPEDA_BERRIES.item().createItemStack(count))
    override fun getLeafDrops(count: Int, random: Random) = listOf(MaterialCard.VEROPEDA_LEAF.item().createItemStack(count))

    override val possibleTraits = super.possibleTraits + setOf()

    val VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("veropeda_cluster")
    val LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("large_veropeda_cluster")
    val VEROPEDA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("veropeda_cluster")
    val NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("nether_veropeda_cluster")

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Artificial Plant Species", "人造植物"), // TODO 未使用ポエム：人間が作り出したモンスター
        description = EnJa("Harvest Veropeda the cursed grass, which grows in arid biomes and the nether", "乾燥系バイオームとネザーに分布する呪草ヴェロペダを収穫する"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Feature.FLOWER {
            VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it)))
            }) { // 小さな塊
                VEROPEDA_CLUSTER_PLACED_FEATURE_KEY({ per(16) + flower }) { +ConventionalBiomeTags.IS_DRY } // 地上用クラスタ
            }
        }
        Feature.FLOWER {
            LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchFeatureConfig(40, 8, 3, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it)))
            }) { // 大きな塊
                NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY({ per(8) + netherFlower }) { nether } // ネザー用クラスタ
            }
        }

    }
}

class VeropedaBlock(settings: Properties) : SimpleMagicPlantBlock(VeropedaCard, settings) {
    companion object {
        val CODEC: MapCodec<VeropedaBlock> = simpleCodec(::VeropedaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}

object SarraceniaCard : AbstractVeropedaCard<SarraceniaBlock>() {
    override fun getBlockPath() = "sarracenia"
    override val blockName = EnJa("Sarracenia", "瓶子草サラセニア")
    override fun getItemPath() = "sarracenia_bulb"
    override val itemName = EnJa("Sarracenia Bulb", "瓶子草サラセニアの球根")
    override val tier = 1
    override val poem = EnJa("Waiting for a flying creature...", "妖精たちの憩いの場。")

    override val blockCodec = SarraceniaBlock.CODEC
    override fun createBlock() = SarraceniaBlock(createCommonSettings().breakInstantly().mapColor(MapColor.NETHER).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.SARRACENIA_LEAF.item, MaterialCard.FAIRY_SCALES.item)
    override fun getLeafDrops(count: Int, random: RandomSource) = listOf(MaterialCard.SARRACENIA_LEAF.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.FAIRY_SCALES.item().createItemStack(count))

    override val possibleTraits = super.possibleTraits + setOf()

    val SARRACENIA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("sarracenia_cluster")
    val SARRACENIA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("sarracenia_cluster")

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { VeropedaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Temptation of Sugar and Acid", "糖と酸の誘惑"),
        description = EnJa("Find Sarracenia in the swampland", "沼地のサラセニアを見つける"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Feature.FLOWER {
            SARRACENIA_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchConfiguration(20, 8, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(it)))
            }) {
                SARRACENIA_CLUSTER_PLACED_FEATURE_KEY({ per(8) + flower }) { +ConventionalBiomeTags.IS_SWAMP + +ConventionalBiomeTags.IS_JUNGLE }
            }
        }
    }
}

class SarraceniaBlock(settings: Properties) : SimpleMagicPlantBlock(SarraceniaCard, settings) {
    companion object {
        val CODEC: MapCodec<SarraceniaBlock> = simpleCodec(::SarraceniaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
