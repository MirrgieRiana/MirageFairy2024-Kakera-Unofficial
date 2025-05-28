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
import miragefairy2024.util.Registration
import miragefairy2024.util.count
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.end
import miragefairy2024.util.flower
import miragefairy2024.util.get
import miragefairy2024.util.nether
import miragefairy2024.util.netherFlower
import miragefairy2024.util.not
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.biome.Biomes as BiomeKeys
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig

object MirageFlowerCard : SimpleMagicPlantCard<MirageFlowerBlock>() {
    override val blockPath = "mirage_flower"
    override val blockName = EnJa("Mirage Flower", "妖花ミラージュ")
    override val itemPath = "mirage_bulb"
    override val itemName = EnJa("Mirage Bulb", "ミラージュの球根")
    override val tier = 1
    override val poem = EnJa("Evolution to escape extermination", "可憐にして人畜無害たる魔物。")
    override val classification = EnJa("Order Miragales, family Miragaceae", "妖花目ミラージュ科")

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
    override fun createBlock() = MirageFlowerBlock(createCommonSettings().breakInstantly().mapColor(MapColor.DIAMOND).sound(BlockSoundGroup.GLASS))

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(6.0, 12.0),
        createCuboidShape(6.0, 15.0),
        createCuboidShape(6.0, 16.0),
    )

    override val drops = listOf(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.MIRAGE_LEAVES.item, MaterialCard.FAIRY_CRYSTAL.item)
    override fun getFruitDrops(count: Int, random: Random) = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: Random) = listOf(MaterialCard.MIRAGE_LEAVES.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(MaterialCard.FAIRY_CRYSTAL.item().createItemStack(count))

    override val family = MirageFairy2024.identifier("mirage")
    override val possibleTraits = setOf(
        TraitCard.ETHER_RESPIRATION.trait, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
        TraitCard.AIR_ADAPTATION.trait, // 空間適応
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
        TraitCard.FAIRY_BLESSING.trait, // 妖精の祝福
        TraitCard.FOUR_LEAFED.trait, // 四つ葉
        TraitCard.NODED_STEM.trait, // 節状の茎
        TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        //TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        TraitCard.DESERT_GEM.trait, // 砂漠の宝石
        TraitCard.HEATING_MECHANISM.trait, // 発熱機構
        TraitCard.WATERLOGGING_TOLERANCE.trait, // 浸水耐性
        TraitCard.ADVERSITY_FLOWER.trait, // 高嶺の花
        TraitCard.FLESHY_LEAVES.trait, // 肉厚の葉
        TraitCard.NATURAL_ABSCISSION.trait, // 自然落果
        TraitCard.CARNIVOROUS_PLANT.trait, // 食虫植物
        TraitCard.ETHER_PREDATION.trait, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait, // アスファルトに咲く花
        TraitCard.PROSPERITY_OF_SPECIES.trait, // 種の繁栄
        //TraitCard.PHANTOM_FLOWER.trait, // 幻の花
        //TraitCard.ETERNAL_TREASURE.trait, // 悠久の秘宝
        //TraitCard.TREASURE_OF_XARPA.trait, // シャルパの秘宝
        TraitCard.CROSSBREEDING.trait, // 交雑
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait, // 自我を持つ植物
        //TraitCard.FLOWER_OF_THE_END.trait, // 終焉の花
    )

    val FAIRY_RING_FEATURE = FairyRingFeature(FairyRingFeatureConfig.CODEC)
    val MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("mirage_cluster")
    val LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("large_mirage_cluster")
    val MIRAGE_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("mirage_cluster")
    val NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("nether_mirage_cluster")
    val MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("mirage_cluster_fairy_forest")
    val LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("large_mirage_cluster")

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("A World Ruled by Plants", "植物の支配する世界"),
        //name = EnJa("Terraformer of the Fantasy World", "幻想世界のテラフォーマー"), // TODO どこかで使う
        description = EnJa("Find the Mirage flower", "妖花ミラージュを摘もう"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("mirage_flower")) { MirageFlowerBlock.CODEC }.register()

        // 地形生成
        run {

            // Fairy Ring Feature
            Registration(BuiltInRegistries.FEATURE, MirageFairy2024.identifier("fairy_ring")) { FAIRY_RING_FEATURE }.register()

            // 小さな塊ConfiguredFeature
            registerDynamicGeneration(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.simple(block().withAge(block().maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // Fairy Ring ConfiguredFeature
            registerDynamicGeneration(LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.simple(block().withAge(block().maxAge))
                FAIRY_RING_FEATURE with FairyRingFeatureConfig(100, 6F, 8F, 3, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 地上とエンド用PlacedFeature
            registerDynamicGeneration(MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(16) + flower }
                Registries.CONFIGURED_FEATURE[MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY] with placementModifiers
            }

            // ネザー用PlacedFeature
            registerDynamicGeneration(NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(64) + netherFlower }
                Registries.CONFIGURED_FEATURE[MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY] with placementModifiers
            }

            // 妖精の森用PlacedFeature
            registerDynamicGeneration(MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { count(4) + flower }
                Registries.CONFIGURED_FEATURE[MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY] with placementModifiers
            }

            // Fairy Ring PlacedFeature
            registerDynamicGeneration(LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(600) + flower }
                Registries.CONFIGURED_FEATURE[LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY] with placementModifiers
            }

            MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { overworld + end * !+BiomeKeys.THE_END } // 地上・エンド外縁の島々に通常クラスタ
            NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { nether } // ネザーにネザー用クラスタ
            LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { overworld } // 地上にFairy Ring

        }

    }
}

class MirageFlowerBlock(settings: Properties) : SimpleMagicPlantBlock(MirageFlowerCard, settings) {
    companion object {
        val CODEC: MapCodec<MirageFlowerBlock> = simpleCodec(::MirageFlowerBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}

fun getMirageFlour(count: Int, random: Random): List<ItemStack> {
    var count2 = count.toDouble()
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_NATURE.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_EARTH.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_SKY.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    return listOf(MaterialCard.MIRAGE_FLOUR_OF_TIME.item().createItemStack(random.randomInt(count2)))
}
