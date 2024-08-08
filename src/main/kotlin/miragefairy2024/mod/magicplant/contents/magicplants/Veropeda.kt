package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.WorldGenTraitRecipeInitScope
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.nether
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.tag
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.MapColor
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier
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

    override fun getFruitDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.VEROPEDA_BERRIES.item.createItemStack(count))
    override fun getLeafDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.VEROPEDA_LEAF.item.createItemStack(count))

    val VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY: RegistryKey<ConfiguredFeature<*, *>> = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "veropeda_cluster"))
    val LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY: RegistryKey<ConfiguredFeature<*, *>> = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "large_veropeda_cluster"))
    val VEROPEDA_CLUSTER_PLACED_FEATURE_KEY: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "veropeda_cluster"))
    val NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY: RegistryKey<PlacedFeature> = RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "nether_veropeda_cluster"))

    context(ModContext)
    override fun init() {
        super.init()

        // 地形生成
        run {

            // 小さな塊
            registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 大きな塊
            registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(40, 8, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 地上
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, VEROPEDA_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(16),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            // ネザー
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(8),
                    CountMultilayerPlacementModifier.of(1),
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(LARGE_VEROPEDA_CLUSTER_CONFIGURED_FEATURE_KEY) with placementModifiers
            }

            VEROPEDA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { tag(ConventionalBiomeTags.CLIMATE_DRY) } // 地上用クラスタ
            NETHER_VEROPEDA_CLUSTER_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { nether } // ネザー用クラスタ

        }

        // 特性
        ModEvents.onInitialize {
            WorldGenTraitRecipeInitScope(card.block).run {

                // 標準特性
                registerWorldGenTraitRecipe("A.RS", TraitCard.PAVEMENT_FLOWERS) // アスファルトに咲く花
                registerWorldGenTraitRecipe("A.RS", TraitCard.OSMOTIC_ABSORPTION) // 浸透吸収
                registerWorldGenTraitRecipe("A.RS", TraitCard.ARID_ADAPTATION) // 乾燥適応
                registerWorldGenTraitRecipe("..CR", TraitCard.SEEDS_PRODUCTION) // 種子生成
                registerWorldGenTraitRecipe("C.CR", TraitCard.FRUITS_PRODUCTION) // 果実生成
                registerWorldGenTraitRecipe("C.CR", TraitCard.LEAVES_PRODUCTION) // 葉面生成
                registerWorldGenTraitRecipe("..CR", TraitCard.GOLDEN_APPLE) // 金のリンゴ

                // N特性
                registerWorldGenTraitRecipe("NRS.", TraitCard.PHAEOSYNTHESIS) // 闇合成
                registerWorldGenTraitRecipe("NRS.", TraitCard.CARNIVOROUS_PLANT) // 食虫植物
                registerWorldGenTraitRecipe("NRS.", TraitCard.PROSPERITY_OF_SPECIES) // 種の繁栄
                registerWorldGenTraitRecipe("NRS.", TraitCard.FOUR_LEAFED) // 四つ葉
                registerWorldGenTraitRecipe("NRS.", TraitCard.NATURAL_ABSCISSION) // 自然落果
                registerWorldGenTraitRecipe("..NR", TraitCard.PHOTOSYNTHESIS) // 光合成
                registerWorldGenTraitRecipe("..NR", TraitCard.EXPERIENCE_PRODUCTION) // 経験値生成
                registerWorldGenTraitRecipe("..NR", TraitCard.FRUIT_OF_KNOWLEDGE) // 知識の果実
                registerWorldGenTraitRecipe("..NR", TraitCard.SPINY_LEAVES) // 棘状の葉
                registerWorldGenTraitRecipe("..NR", TraitCard.DESERT_GEM) // 砂漠の宝石
                registerWorldGenTraitRecipe("..NR", TraitCard.ADVERSITY_FLOWER) // 高嶺の花
                registerWorldGenTraitRecipe("..NR", TraitCard.FLESHY_LEAVES) // 肉厚の葉

                // SR特性
                registerWorldGenTraitRecipe(".S..", TraitCard.COLD_ADAPTATION) // 寒冷適応
                registerWorldGenTraitRecipe(".S..", TraitCard.WARM_ADAPTATION) // 温暖適応
                registerWorldGenTraitRecipe(".S..", TraitCard.HOT_ADAPTATION) // 熱帯適応
                registerWorldGenTraitRecipe(".S..", TraitCard.ARID_ADAPTATION) // 乾燥適応
                registerWorldGenTraitRecipe(".S..", TraitCard.MESIC_ADAPTATION) // 中湿適応
                registerWorldGenTraitRecipe(".S..", TraitCard.HUMID_ADAPTATION) // 湿潤適応

            }
        }

        // レシピ
        card.item.registerHarvestNotation(MaterialCard.VEROPEDA_BERRIES.item, MaterialCard.VEROPEDA_LEAF.item)

    }
}

object VeropedaCard : SimpleMagicPlantCard<VeropedaBlock>(VeropedaSettings)

class VeropedaBlock(settings: Settings) : SimpleMagicPlantBlock(VeropedaSettings, settings) {
    override fun getAgeProperty(): IntProperty = Properties.AGE_3
}
