package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.BiomeCards
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.getOr
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.undergroundFlower
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.biome.Biomes as BiomeKeys
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig

object EmeraldLuminariaCard : SimpleMagicPlantCard<EmeraldLuminariaBlock>() {
    override fun getBlockPath() = "emerald_luminaria"
    override val blockName = EnJa("Emerald Luminaria", "翠玉輝草エメラルドルミナリア")
    override fun getItemPath() = "emerald_luminaria_bulb"
    override val itemName = EnJa("Emerald Luminaria Bulb", "エメラルドルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Makes Berryllium by unknown means", "幸福もたらす、栄光の樹。")
    override val classification = EnJa("Order Miragales, family Luminariaceae", "妖花目ルミナリア科")

    override val ageProperty: IntProperty = BlockStateProperties.AGE_3
    override fun createBlock() = EmeraldLuminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.EMERALD).sound(BlockSoundGroup.CROP))

    override val outlineShapes = listOf(
        createCuboidShape(4.0, 6.0),
        createCuboidShape(5.0, 13.0),
        createCuboidShape(7.0, 16.0),
        createCuboidShape(7.0, 16.0),
    )

    override val baseGrowth = 0.2
    override val baseFruitGeneration = 0.1

    override val drops = listOf(MaterialCard.LUMINITE.item, { Items.EMERALD })
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.LUMINITE.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(Items.EMERALD.createItemStack(count))

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
        TraitCard.FOUR_LEAFED.trait, // 四つ葉
        //TraitCard.NODED_STEM.trait, // 節状の茎
        //TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        //TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        //TraitCard.DESERT_GEM.trait, // 砂漠の宝石
        //TraitCard.HEATING_MECHANISM.trait, // 発熱機構
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

    val EMERALD_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("emerald_luminaria_cluster")
    val EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("emerald_luminaria_cluster")
    val UNDERGROUND_EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("underground_emerald_luminaria_cluster")

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Money Tree", "金のなる木"),
        description = EnJa("Search for Emerald Luminaria in a plant-rich biome", "植物の繁茂するバイオームでエメラルドルミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("emerald_luminaria")) { EmeraldLuminariaBlock.CODEC }.register()

        Feature.FLOWER {
            EMERALD_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchFeatureConfig(1, 0, 0, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it)))
            }) {
                EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY({ per(32) + flower }) { +ConventionalBiomeTags.IS_JUNGLE + +BiomeCards.FAIRY_FOREST.registryKey }  // 地上 // TODO 妖精の森が強すぎる
                UNDERGROUND_EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY({ count(32) + undergroundFlower }) { +BiomeKeys.LUSH_CAVES } // 地下
            }
        }

    }
}

class EmeraldLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(EmeraldLuminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<EmeraldLuminariaBlock> = simpleCodec(::EmeraldLuminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}
