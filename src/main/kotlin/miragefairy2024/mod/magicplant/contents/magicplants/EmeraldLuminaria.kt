package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.BiomeCards
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.count
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.getOr
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.undergroundFlower
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.MapColor
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.random.Random
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.stateprovider.BlockStateProvider

object EmeraldLuminariaSettings : SimpleMagicPlantSettings<EmeraldLuminariaCard, EmeraldLuminariaBlock>() {
    override val card get() = EmeraldLuminariaCard

    override val blockPath = "emerald_luminaria"
    override val blockName = EnJa("Emerald Luminaria", "翠玉輝草エメラルドルミナリア")
    override val itemPath = "emerald_luminaria_bulb"
    override val itemName = EnJa("Emerald Luminaria Bulb", "エメラルドルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Makes Berryllium by unknown means", "幸福もたらす、栄光の樹。")
    override val classification = EnJa("Order Miragales, family Luminariaceae", "妖花目ルミナリア科")

    override fun createBlock() = EmeraldLuminariaBlock(createCommonSettings().strength(0.2F).luminance { getLuminance(it.getOr(Properties.AGE_3) { 0 }) }.mapColor(MapColor.EMERALD_GREEN).sounds(BlockSoundGroup.CROP))

    override val outlineShapes = listOf(
        createCuboidShape(4.0, 6.0),
        createCuboidShape(5.0, 13.0),
        createCuboidShape(7.0, 16.0),
        createCuboidShape(7.0, 16.0),
    )

    override val drops = listOf(Items.EMERALD)
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

    val EMERALD_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY = RegistryKeys.CONFIGURED_FEATURE with MirageFairy2024.identifier("emerald_luminaria_cluster")
    val EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("emerald_luminaria_cluster")
    val UNDERGROUND_EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("underground_emerald_luminaria_cluster")

    context(ModContext)
    override fun init() {
        super.init()

        // Configured Feature
        registerDynamicGeneration(EMERALD_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY) {
            val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
            Feature.FLOWER with RandomPatchFeatureConfig(1, 0, 0, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
        }

        // 地上に配置
        registerDynamicGeneration(EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY) {
            val placementModifiers = placementModifiers { per(32) + flower }
            it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(EMERALD_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
        }
        EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +ConventionalBiomeTags.JUNGLE }
        EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +BiomeCards.FAIRY_FOREST.registryKey }

        // 地下に配置
        registerDynamicGeneration(UNDERGROUND_EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY) {
            val placementModifiers = placementModifiers { count(32) + undergroundFlower }
            it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(EMERALD_LUMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
        }
        UNDERGROUND_EMERALD_LUMINARIA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +BiomeKeys.LUSH_CAVES }

    }
}

object EmeraldLuminariaCard : SimpleMagicPlantCard<EmeraldLuminariaBlock>(EmeraldLuminariaSettings)

class EmeraldLuminariaBlock(settings: Settings) : SimpleMagicPlantBlock(EmeraldLuminariaSettings, settings) {
    override fun getAgeProperty(): IntProperty = Properties.AGE_3
}
