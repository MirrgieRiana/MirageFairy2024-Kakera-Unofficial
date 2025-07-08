package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.item.MaterialCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.center
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.nether
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
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

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b00101000, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b00101000, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0b00101000, // 葉面生成
        TraitCard.CARNIVOROUS_PLANT.trait to 0b00101000, // 食虫植物
        TraitCard.PAVEMENT_FLOWERS.trait to 0b00101000, // アスファルトに咲く花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0.05, // 葉面生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.NATURAL_ABSCISSION.trait to 0.05, // 自然落果
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait to 0.05, // 闇合成
        TraitCard.FOUR_LEAFED.trait to 0.05, // 四つ葉
        TraitCard.FRUIT_OF_KNOWLEDGE.trait to 0.05, // 禁断の果実
        TraitCard.SPINY_LEAVES.trait to 0.05, // 棘のある葉
        TraitCard.FLESHY_LEAVES.trait to 0.05, // 肉厚の葉
        TraitCard.CARNIVOROUS_PLANT.trait to 0.05, // 食虫植物
        TraitCard.PAVEMENT_FLOWERS.trait to 0.05, // アスファルトに咲く花
    )

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
            configuredFeature("cluster", { RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) { // 小さな塊
                placedFeature("cluster", { per(16) + flower(square, surface) }) { +ConventionalBiomeTags.IS_DESERT + +ConventionalBiomeTags.IS_SAVANNA + +ConventionalBiomeTags.IS_BADLANDS } // 地上用クラスタ
            }
            configuredFeature("large_cluster", { RandomPatchFeatureConfig(40, 8, 3, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) { // 大きな塊
                placedFeature("nether_cluster", { per(8) + flower(center, nether) }) { nether } // ネザー用クラスタ
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

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0b00101000, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0b00101000, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.LEAVES_PRODUCTION.trait to 0b00101000, // 葉面生成
        TraitCard.PHOTOSYNTHESIS.trait to 0b00101000, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.WATERLOGGING_TOLERANCE.trait to 0b00101000, // 浸水耐性
        TraitCard.CARNIVOROUS_PLANT.trait to 0b00101000, // 食虫植物
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.LEAVES_PRODUCTION.trait to 0.05, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.FOUR_LEAFED.trait to 0.05, // 四つ葉
        TraitCard.NODED_STEM.trait to 0.05, // 節状の茎
        TraitCard.WATERLOGGING_TOLERANCE.trait to 0.05, // 浸水耐性
        TraitCard.CARNIVOROUS_PLANT.trait to 0.05, // 食虫植物
    )

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
            configuredFeature("cluster", { RandomPatchConfiguration(20, 8, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(it))) }) {
                placedFeature("cluster", { per(8) + flower(center, surface) }) { +ConventionalBiomeTags.IS_SWAMP + +ConventionalBiomeTags.IS_JUNGLE }
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
