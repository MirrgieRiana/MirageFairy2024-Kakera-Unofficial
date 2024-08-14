package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.count
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.end
import miragefairy2024.util.flower
import miragefairy2024.util.nether
import miragefairy2024.util.netherFlower
import miragefairy2024.util.not
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.minecraft.block.MapColor
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.stateprovider.BlockStateProvider

object MirageFlowerSettings : SimpleMagicPlantSettings<MirageFlowerCard, MirageFlowerBlock>() {
    override val card get() = MirageFlowerCard

    override val blockPath = "mirage_flower"
    override val blockEnName = "Mirage Flower"
    override val blockJaName = "妖花ミラージュ"
    override val itemPath = "mirage_bulb"
    override val itemEnName = "Mirage Bulb"
    override val itemJaName = "ミラージュの球根"
    override val tier = 1
    override val enPoem = "Evolution to escape extermination"
    override val jaPoem = "可憐にして人畜無害たる魔物。"
    override val enClassification = "Order Miragales, family Miragaceae"
    override val jaClassification = "妖花目ミラージュ科"

    override fun createBlock() = MirageFlowerBlock(createCommonSettings().breakInstantly().mapColor(MapColor.DIAMOND_BLUE).sounds(BlockSoundGroup.GLASS))

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(6.0, 12.0),
        createCuboidShape(6.0, 15.0),
        createCuboidShape(6.0, 16.0),
    )

    override val drops = listOf(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.MIRAGE_LEAVES.item, MaterialCard.FAIRY_CRYSTAL.item)
    override fun getFruitDrops(count: Int, random: Random): List<ItemStack> = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.MIRAGE_LEAVES.item.createItemStack(count))
    override fun getRareDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.FAIRY_CRYSTAL.item.createItemStack(count))

    override val family = Identifier(MirageFairy2024.modId, "mirage")
    override val possibleTraits = setOf(
        TraitCard.ETHER_RESPIRATION.trait, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 浸透吸収
        TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
        TraitCard.AIR_ADAPTATION.trait, // 空気適応
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
        TraitCard.FAIRY_BLESSING.trait, // 妖精の祝福
        TraitCard.FOUR_LEAFED.trait, // 四つ葉
        TraitCard.NODED_STEM.trait, // 節状の茎
        //TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 知識の果実
        //TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        TraitCard.SPINY_LEAVES.trait, // 棘状の葉
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
    )

    val FAIRY_RING_FEATURE = FairyRingFeature(FairyRingFeatureConfig.CODEC)
    val MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY = RegistryKeys.CONFIGURED_FEATURE with Identifier(MirageFairy2024.modId, "mirage_cluster")
    val LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY = RegistryKeys.CONFIGURED_FEATURE with Identifier(MirageFairy2024.modId, "large_mirage_cluster")
    val MIRAGE_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with Identifier(MirageFairy2024.modId, "mirage_cluster")
    val NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with Identifier(MirageFairy2024.modId, "nether_mirage_cluster")
    val MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with Identifier(MirageFairy2024.modId, "mirage_cluster_fairy_forest")
    val LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with Identifier(MirageFairy2024.modId, "large_mirage_cluster")

    context(ModContext)
    override fun init() {
        super.init()

        // 地形生成
        run {

            // Fairy Ring Feature
            FAIRY_RING_FEATURE.register(Registries.FEATURE, Identifier(MirageFairy2024.modId, "fairy_ring"))

            // 小さな塊ConfiguredFeature
            registerDynamicGeneration(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // Fairy Ring ConfiguredFeature
            registerDynamicGeneration(LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                FAIRY_RING_FEATURE with FairyRingFeatureConfig(100, 6F, 8F, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 地上とエンド用PlacedFeature
            registerDynamicGeneration(MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(16) + flower }
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // ネザー用PlacedFeature
            registerDynamicGeneration(NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(64) + netherFlower }
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // 妖精の森用PlacedFeature
            registerDynamicGeneration(MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { count(4) + flower }
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // Fairy Ring PlacedFeature
            registerDynamicGeneration(LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = placementModifiers { per(600) + flower }
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { overworld } // 地上に通常クラスタ
            MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { end * !+BiomeKeys.THE_END } // エンド外縁の島々に通常クラスタ
            NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { nether } // ネザーにネザー用クラスタ
            LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { overworld } // 地上にFairy Ring

        }

    }
}

object MirageFlowerCard : SimpleMagicPlantCard<MirageFlowerBlock>(MirageFlowerSettings)

class MirageFlowerBlock(settings: Settings) : SimpleMagicPlantBlock(MirageFlowerSettings, settings) {
    override fun getAgeProperty(): IntProperty = Properties.AGE_3
}

private fun getMirageFlour(count: Int, random: Random): List<ItemStack> {
    var count2 = count.toDouble()
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR.item.createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_NATURE.item.createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_EARTH.item.createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item.createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_SKY.item.createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item.createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    return listOf(MaterialCard.MIRAGE_FLOUR_OF_TIME.item.createItemStack(random.randomInt(count2)))
}
