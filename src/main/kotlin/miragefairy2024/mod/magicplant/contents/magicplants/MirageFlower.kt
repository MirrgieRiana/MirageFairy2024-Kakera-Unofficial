package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.WorldGenTraitRecipe
import miragefairy2024.mod.magicplant.WorldGenTraitRecipeInitScope
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.biome
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.end
import miragefairy2024.util.nether
import miragefairy2024.util.not
import miragefairy2024.util.overworld
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.MapColor
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier
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

    override fun getFruitDrops(count: Int, random: Random): List<ItemStack> = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.MIRAGE_LEAVES.item.createItemStack(count))
    override fun getRareDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.FAIRY_CRYSTAL.item.createItemStack(count))

    val FAIRY_RING_FEATURE = FairyRingFeature(FairyRingFeatureConfig.CODEC)
    val MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY: RegistryKey<ConfiguredFeature<*, *>> = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "mirage_cluster"))
    val LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY: RegistryKey<ConfiguredFeature<*, *>> = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "large_mirage_cluster"))
    val MIRAGE_CLUSTER_PLACED_FEATURE_KEY: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "mirage_cluster"))
    val NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "nether_mirage_cluster"))
    val MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "mirage_cluster_fairy_forest"))
    val LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "large_mirage_cluster"))

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
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(16),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // ネザー用PlacedFeature
            registerDynamicGeneration(NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(64),
                    CountMultilayerPlacementModifier.of(1),
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // 妖精の森用PlacedFeature
            registerDynamicGeneration(MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY) {
                val placementModifiers = listOf(
                    CountPlacementModifier.of(4),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // Fairy Ring PlacedFeature
            registerDynamicGeneration(LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(600),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(LARGE_MIRAGE_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { overworld } // 地上に通常クラスタ
            MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { end * !biome(BiomeKeys.THE_END) } // エンド外縁の島々に通常クラスタ
            NETHER_MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { nether } // ネザーにネザー用クラスタ
            LARGE_MIRAGE_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { overworld } // 地上にFairy Ring

        }

        // 特性
        ModEvents.onInitialize {
            WorldGenTraitRecipeInitScope(card.block).run {

                // 標準特性
                registerWorldGenTraitRecipe("A.RS", TraitCard.ETHER_RESPIRATION) // エーテル呼吸
                registerWorldGenTraitRecipe("A.RS", TraitCard.AIR_ADAPTATION) // 空気適応
                registerWorldGenTraitRecipe("..CR", TraitCard.SEEDS_PRODUCTION) // 種子生成
                registerWorldGenTraitRecipe("C.CR", TraitCard.FRUITS_PRODUCTION) // 果実生成
                registerWorldGenTraitRecipe("..CR", TraitCard.LEAVES_PRODUCTION) // 葉面生成
                registerWorldGenTraitRecipe("C.CR", TraitCard.RARE_PRODUCTION) // 希少品生成
                registerWorldGenTraitRecipe("..CR", TraitCard.FAIRY_BLESSING) // 妖精の祝福

                // R特性
                registerWorldGenTraitRecipe("..RS", TraitCard.PHOTOSYNTHESIS) // 光合成
                registerWorldGenTraitRecipe("..RS", TraitCard.OSMOTIC_ABSORPTION) // 浸透吸収
                registerWorldGenTraitRecipe("RS..", TraitCard.CRYSTAL_ABSORPTION) // 鉱物吸収
                registerWorldGenTraitRecipe("..RS", TraitCard.EXPERIENCE_PRODUCTION) // 経験値生成

                // SR特性
                registerWorldGenTraitRecipe(".S..", TraitCard.PHAEOSYNTHESIS) // 闇合成

                // 環境依存特性
                registerWorldGenTraitRecipe(".A..", TraitCard.ETHER_RESPIRATION, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.IN_THE_END)) // エーテル呼吸
                registerWorldGenTraitRecipe(".A..", TraitCard.AIR_ADAPTATION, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.IN_THE_END)) // 空気適応
                registerWorldGenTraitRecipe(".CRS", TraitCard.COLD_ADAPTATION, WorldGenTraitRecipe.Condition.Temperature(TemperatureCategory.LOW)) // 寒冷適応
                registerWorldGenTraitRecipe(".CRS", TraitCard.WARM_ADAPTATION, WorldGenTraitRecipe.Condition.Temperature(TemperatureCategory.MEDIUM)) // 温暖適応
                registerWorldGenTraitRecipe(".CRS", TraitCard.HOT_ADAPTATION, WorldGenTraitRecipe.Condition.Temperature(TemperatureCategory.HIGH)) // 熱帯適応
                registerWorldGenTraitRecipe(".CRS", TraitCard.ARID_ADAPTATION, WorldGenTraitRecipe.Condition.Humidity(HumidityCategory.LOW)) // 乾燥適応
                registerWorldGenTraitRecipe(".CRS", TraitCard.MESIC_ADAPTATION, WorldGenTraitRecipe.Condition.Humidity(HumidityCategory.MEDIUM)) // 中湿適応
                registerWorldGenTraitRecipe(".CRS", TraitCard.HUMID_ADAPTATION, WorldGenTraitRecipe.Condition.Humidity(HumidityCategory.HIGH)) // 湿潤適応

                // バイオーム限定特性
                registerWorldGenTraitRecipe(".CRS", TraitCard.FOUR_LEAFED, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.FLORAL)) // 四つ葉
                registerWorldGenTraitRecipe(".CRS", TraitCard.NODED_STEM, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.BEACH)) // 節状の茎
                registerWorldGenTraitRecipe(".CRS", TraitCard.FRUIT_OF_KNOWLEDGE, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.JUNGLE)) // 知識の果実
                registerWorldGenTraitRecipe(".CRS", TraitCard.GOLDEN_APPLE, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.FOREST)) // 金のリンゴ
                registerWorldGenTraitRecipe(".CRS", TraitCard.SPINY_LEAVES, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.MESA)) // 棘状の葉
                registerWorldGenTraitRecipe(".CRS", TraitCard.DESERT_GEM, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.DESERT)) // 砂漠の宝石
                registerWorldGenTraitRecipe(".CRS", TraitCard.HEATING_MECHANISM, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.SNOWY)) // 発熱機構
                registerWorldGenTraitRecipe(".CRS", TraitCard.WATERLOGGING_TOLERANCE, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.RIVER)) // 浸水耐性
                registerWorldGenTraitRecipe(".CRS", TraitCard.ADVERSITY_FLOWER, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.MOUNTAIN)) // 高嶺の花
                registerWorldGenTraitRecipe(".CRS", TraitCard.FLESHY_LEAVES, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.SAVANNA)) // 肉厚の葉
                registerWorldGenTraitRecipe(".CRS", TraitCard.NATURAL_ABSCISSION, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.TAIGA)) // 自然落果
                registerWorldGenTraitRecipe(".CRS", TraitCard.CARNIVOROUS_PLANT, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.SWAMP)) // 食虫植物
                registerWorldGenTraitRecipe(".CRS", TraitCard.ETHER_PREDATION, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.IN_THE_END)) // エーテル捕食
                registerWorldGenTraitRecipe(".CRS", TraitCard.PAVEMENT_FLOWERS, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.IN_NETHER)) // アスファルトに咲く花
                registerWorldGenTraitRecipe(".CRS", TraitCard.PROSPERITY_OF_SPECIES, WorldGenTraitRecipe.Condition.InBiome(ConventionalBiomeTags.PLAINS)) // 種の繁栄

            }
        }

        // レシピ
        card.item.registerHarvestNotation(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.MIRAGE_LEAVES.item, MaterialCard.FAIRY_CRYSTAL.item)

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
