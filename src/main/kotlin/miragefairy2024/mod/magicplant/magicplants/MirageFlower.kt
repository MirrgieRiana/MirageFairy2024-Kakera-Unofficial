package miragefairy2024.mod.magicplant.magicplants

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.magicplant.MagicPlantBlockEntity
import miragefairy2024.mod.magicplant.MagicPlantCard
import miragefairy2024.mod.magicplant.SimpleMagicPlantBlock
import miragefairy2024.mod.magicplant.TraitCard
import miragefairy2024.mod.magicplant.WorldGenTraitRecipe
import miragefairy2024.mod.magicplant.WorldGenTraitRecipeInitScope
import miragefairy2024.mod.magicplant.initMagicPlant
import miragefairy2024.mod.magicplant.registerMagicPlantDropNotation
import miragefairy2024.mod.poem
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.concat
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.with
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.data.client.Models
import net.minecraft.data.client.TextureKey
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.FeatureConfig
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.feature.util.FeatureContext
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier
import net.minecraft.world.gen.stateprovider.BlockStateProvider

object MirageFlowerCard : MagicPlantCard<MirageFlowerBlock, MirageFlowerBlockEntity>(
    "mirage_flower", "Mirage Flower", "妖花ミラージュ",
    "mirage_bulb", "Mirage Bulb", "ミラージュの球根",
    PoemList(1)
        .poem("Evolution to escape extermination", "可憐にして人畜無害たる魔物。")
        .poem("classification", "Order Miragales, family Miragaceae", "妖花目ミラージュ科"),
    { MirageFlowerBlock(createCommonSettings().breakInstantly().mapColor(MapColor.DIAMOND_BLUE).sounds(BlockSoundGroup.GLASS)) },
    ::MirageFlowerBlockEntity,
)

val fairyRingFeature = FairyRingFeature(FairyRingFeatureConfig.CODEC)
val mirageClusterConfiguredFeatureKey: RegistryKey<ConfiguredFeature<*, *>> = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "mirage_cluster"))
val largeMirageClusterConfiguredFeatureKey: RegistryKey<ConfiguredFeature<*, *>> = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "large_mirage_cluster"))
val mirageClusterPlacedFeatureKey: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "mirage_cluster"))
val netherMirageClusterPlacedFeatureKey: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "nether_mirage_cluster"))
val mirageClusterFairyForestPlacedFeatureKey: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "mirage_cluster_fairy_forest"))
val largeMirageClusterPlacedFeatureKey: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "large_mirage_cluster"))

fun initMirageFlower() {
    val card = MirageFlowerCard
    card.initMagicPlant()

    ModEvents.onInitialize {

        // 見た目
        card.block.registerVariantsBlockStateGeneration { normal("block/" concat card.blockIdentifier) with card.block.ageProperty }
        card.block.ageProperty.values.forEach { age ->
            val texturedModel = Models.CROSS.with(TextureKey.CROSS to ("block/" concat card.blockIdentifier concat "_age$age"))
            texturedModel.registerModelGeneration("block/" concat card.blockIdentifier concat "_age$age")
        }

    }

    // 地形生成
    run {

        // Fairy Ring Feature
        fairyRingFeature.register(Registries.FEATURE, Identifier(MirageFairy2024.modId, "fairy_ring"))

        ModEvents.onInitialize {

            // 小さな塊ConfiguredFeature
            registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, mirageClusterConfiguredFeatureKey) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // Fairy Ring ConfiguredFeature
            registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, largeMirageClusterConfiguredFeatureKey) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                fairyRingFeature with FairyRingFeatureConfig(100, 6F, 8F, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 地上とエンド用PlacedFeature
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, mirageClusterPlacedFeatureKey) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(16),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(mirageClusterConfiguredFeatureKey) with placementModifiers
            }

            // ネザー用PlacedFeature
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, netherMirageClusterPlacedFeatureKey) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(64),
                    CountMultilayerPlacementModifier.of(1),
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(mirageClusterConfiguredFeatureKey) with placementModifiers
            }

            // 妖精の森用PlacedFeature
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, mirageClusterFairyForestPlacedFeatureKey) {
                val placementModifiers = listOf(
                    CountPlacementModifier.of(4),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(mirageClusterConfiguredFeatureKey) with placementModifiers
            }

            // Fairy Ring PlacedFeature
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, largeMirageClusterPlacedFeatureKey) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(600),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(largeMirageClusterConfiguredFeatureKey) with placementModifiers
            }

            // 地上とエンドに配置
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.VEGETAL_DECORATION, mirageClusterPlacedFeatureKey)
            BiomeModifications.addFeature(BiomeSelectors.foundInTheEnd().and(BiomeSelectors.excludeByKey(BiomeKeys.THE_END)), GenerationStep.Feature.VEGETAL_DECORATION, mirageClusterPlacedFeatureKey)

            // ネザーに配置
            BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), GenerationStep.Feature.VEGETAL_DECORATION, netherMirageClusterPlacedFeatureKey)

            // 地上にFairy Ringを配置
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.VEGETAL_DECORATION, largeMirageClusterPlacedFeatureKey)

        }
    }

    ModEvents.onInitialize {

        // 特性
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

        // レシピ
        registerMagicPlantDropNotation(card.item, MaterialCard.MIRAGE_FLOUR.item, MaterialCard.MIRAGE_LEAVES.item, MaterialCard.FAIRY_CRYSTAL.item)

    }
}

@Suppress("OVERRIDE_DEPRECATION")
class MirageFlowerBlock(settings: Settings) : SimpleMagicPlantBlock(settings) {
    companion object {
        private val AGE_TO_SHAPE: Array<VoxelShape> = arrayOf(
            createCuboidShape(3.0, 5.0),
            createCuboidShape(6.0, 12.0),
            createCuboidShape(6.0, 15.0),
            createCuboidShape(6.0, 16.0),
        )
    }

    override val ageProperty: IntProperty get() = Properties.AGE_3
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = AGE_TO_SHAPE[getAge(state)]
    override fun createBlockEntity(pos: BlockPos, state: BlockState) = MirageFlowerBlockEntity(pos, state)
    override fun getFruitDrops(count: Int): List<ItemStack> = listOf(MaterialCard.MIRAGE_FLOUR.item.createItemStack(count))
    override fun getLeafDrops(count: Int): List<ItemStack> = listOf(MaterialCard.MIRAGE_LEAVES.item.createItemStack(count))
    override fun getRareDrops(count: Int): List<ItemStack> = listOf(MaterialCard.FAIRY_CRYSTAL.item.createItemStack(count))
}

class MirageFlowerBlockEntity(pos: BlockPos, state: BlockState) : MagicPlantBlockEntity(MirageFlowerCard.blockEntityType, pos, state)

class FairyRingFeatureConfig(val tries: Int, val minRadius: Float, val maxRadius: Float, val ySpread: Int, val feature: RegistryEntry<PlacedFeature>) : FeatureConfig {
    companion object {
        val CODEC: Codec<FairyRingFeatureConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("tries").forGetter(FairyRingFeatureConfig::tries),
                Codec.FLOAT.fieldOf("min_radius").forGetter(FairyRingFeatureConfig::minRadius),
                Codec.FLOAT.fieldOf("max_radius").forGetter(FairyRingFeatureConfig::maxRadius),
                Codec.INT.fieldOf("y_spread").forGetter(FairyRingFeatureConfig::ySpread),
                PlacedFeature.REGISTRY_CODEC.fieldOf("feature").forGetter(FairyRingFeatureConfig::feature),
            ).apply(instance, ::FairyRingFeatureConfig)
        }
    }

    init {
        require(tries >= 0)
        require(minRadius >= 0F)
        require(maxRadius >= 0F)
        require(maxRadius >= minRadius)
        require(ySpread >= 0)
    }
}

class FairyRingFeature(codec: Codec<FairyRingFeatureConfig>) : Feature<FairyRingFeatureConfig>(codec) {
    override fun generate(context: FeatureContext<FairyRingFeatureConfig>): Boolean {
        val config = context.config
        val random = context.random
        val originBlockPos = context.origin
        val world = context.world

        var count = 0
        val minRadius = config.minRadius
        val radiusRange = config.maxRadius - minRadius
        val y1 = config.ySpread + 1
        val mutableBlockPos = BlockPos.Mutable()
        for (l in 0 until config.tries) {
            val r = random.nextFloat() * radiusRange + minRadius
            val theta = random.nextFloat() * MathHelper.TAU
            val x = MathHelper.floor(MathHelper.cos(theta) * r)
            val y = random.nextInt(y1) - random.nextInt(y1)
            val z = MathHelper.floor(MathHelper.sin(theta) * r)

            mutableBlockPos.set(originBlockPos, x, y, z)
            if (config.feature.value().generateUnregistered(world, context.generator, random, mutableBlockPos)) {
                count++
            }
        }

        return count > 0
    }
}
