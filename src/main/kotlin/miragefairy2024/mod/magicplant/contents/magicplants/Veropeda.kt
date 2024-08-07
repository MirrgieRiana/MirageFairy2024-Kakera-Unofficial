package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.MagicPlantCard
import miragefairy2024.mod.magicplant.MagicPlantSettings
import miragefairy2024.mod.magicplant.SimpleMagicPlantBlock
import miragefairy2024.mod.magicplant.WorldGenTraitRecipeInitScope
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.nether
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.tag
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.data.client.Models
import net.minecraft.data.client.TextureKey
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKeys
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier
import net.minecraft.world.gen.stateprovider.BlockStateProvider

object VeropedaSettings : MagicPlantSettings<VeropedaBlock>() {
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

    override fun createBlock() = VeropedaBlock(MagicPlantCard.createCommonSettings().breakInstantly().mapColor(MapColor.DARK_RED).sounds(BlockSoundGroup.CROP))

    context(ModContext)
    override fun init(card: MagicPlantCard<*, VeropedaBlock>) {
        super.init(card)

        // 見た目
        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()) with card.block.ageProperty }
        card.block.ageProperty.values.forEach { age ->
            registerModelGeneration({ "block/" * card.block.getIdentifier() * "_age$age" }) {
                Models.CROSS.with(TextureKey.CROSS to "block/" * card.block.getIdentifier() * "_age$age")
            }
        }

        // 地形生成
        run {

            // 小さな塊
            val veropedaClusterConfiguredFeatureKey = registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "veropeda_cluster")) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 大きな塊
            val largeVeropedaClusterConfiguredFeatureKey = registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "large_veropeda_cluster")) {
                val blockStateProvider = BlockStateProvider.of(card.block.withAge(card.block.maxAge))
                Feature.FLOWER with RandomPatchFeatureConfig(40, 8, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(blockStateProvider)))
            }

            // 地上
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "veropeda_cluster")) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(16),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(veropedaClusterConfiguredFeatureKey) with placementModifiers
            }.also {
                it.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { tag(ConventionalBiomeTags.CLIMATE_DRY) }
            }

            // ネザー
            registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "nether_veropeda_cluster")) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(8),
                    CountMultilayerPlacementModifier.of(1),
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(largeVeropedaClusterConfiguredFeatureKey) with placementModifiers
            }.also {
                it.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { nether }
            }

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

object VeropedaCard : MagicPlantCard<VeropedaSettings, VeropedaBlock>(VeropedaSettings)

@Suppress("OVERRIDE_DEPRECATION")
class VeropedaBlock(settings: Settings) : SimpleMagicPlantBlock({ VeropedaCard }, settings) {
    companion object {
        private val AGE_TO_SHAPE: Array<VoxelShape> = arrayOf(
            createCuboidShape(3.0, 5.0),
            createCuboidShape(4.0, 7.0),
            createCuboidShape(7.0, 9.0),
            createCuboidShape(7.0, 16.0),
        )
    }

    override val ageProperty: IntProperty get() = Properties.AGE_3
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = AGE_TO_SHAPE[getAge(state)]
    override fun getFruitDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.VEROPEDA_BERRIES.item.createItemStack(count))
    override fun getLeafDrops(count: Int, random: Random): List<ItemStack> = listOf(MaterialCard.VEROPEDA_LEAF.item.createItemStack(count))
}
