package miragefairy2024.mod.haimeviska

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.count
import miragefairy2024.util.get
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.tree
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.block.RotatedPillarBlock as PillarBlock
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.core.Direction
import net.minecraft.util.valueproviders.ConstantInt as ConstantIntProvider
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration as TreeFeatureConfig
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer as LargeOakFoliagePlacer
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer as LargeOakTrunkPlacer
import java.util.OptionalInt

object HaimeviskaTreeDecoratorCard {
    val identifier = MirageFairy2024.identifier("haimeviska")
    val treeDecorator = HaimeviskaTreeDecorator()
    private val codec: Codec<HaimeviskaTreeDecorator> = Codec.unit { treeDecorator }
    val type: TreeDecoratorType<HaimeviskaTreeDecorator> = TreeDecoratorType(codec)
}

val HAIMEVISKA_CONFIGURED_FEATURE_KEY = RegistryKeys.CONFIGURED_FEATURE with MirageFairy2024.identifier("haimeviska")
val HAIMEVISKA_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("haimeviska")
val HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("haimeviska_fairy_forest")
val HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY = RegistryKeys.PLACED_FEATURE with MirageFairy2024.identifier("haimeviska_deep_fairy_forest")

context(ModContext)
fun initHaimeviskaWorldGens() {

    // TreeDecoratorの登録
    HaimeviskaTreeDecoratorCard.type.register(Registries.TREE_DECORATOR_TYPE, HaimeviskaTreeDecoratorCard.identifier)

    // ConfiguredFeatureの登録
    registerDynamicGeneration(HAIMEVISKA_CONFIGURED_FEATURE_KEY) {
        Feature.TREE with TreeFeatureConfig.Builder(
            BlockStateProvider.of(HaimeviskaBlockCard.LOG.block),
            LargeOakTrunkPlacer(22, 10, 0), // 最大32
            BlockStateProvider.of(HaimeviskaBlockCard.LEAVES.block),
            LargeOakFoliagePlacer(ConstantIntProvider.create(2), ConstantIntProvider.create(2), 4),
            TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)),
        ).ignoreVines().decorators(listOf(HaimeviskaTreeDecoratorCard.treeDecorator)).build()
    }

    // まばらなPlacedFeature
    registerDynamicGeneration(HAIMEVISKA_PLACED_FEATURE_KEY) {
        val placementModifiers = placementModifiers { per(512) + tree(HaimeviskaBlockCard.SAPLING.block) }
        RegistryKeys.CONFIGURED_FEATURE[HAIMEVISKA_CONFIGURED_FEATURE_KEY] with placementModifiers
    }

    // 高密度のPlacedFeature
    registerDynamicGeneration(HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY) {
        val placementModifiers = placementModifiers { per(16) + tree(HaimeviskaBlockCard.SAPLING.block) }
        RegistryKeys.CONFIGURED_FEATURE[HAIMEVISKA_CONFIGURED_FEATURE_KEY] with placementModifiers
    }

    // 超高密度のPlacedFeature
    registerDynamicGeneration(HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY) {
        val placementModifiers = placementModifiers { count(8) + tree(HaimeviskaBlockCard.SAPLING.block) }
        RegistryKeys.CONFIGURED_FEATURE[HAIMEVISKA_CONFIGURED_FEATURE_KEY] with placementModifiers
    }

    // 平原・森林バイオームに配置
    HAIMEVISKA_PLACED_FEATURE_KEY.registerFeature(GenerationStep.Feature.VEGETAL_DECORATION) { +ConventionalBiomeTags.PLAINS + +ConventionalBiomeTags.FOREST }

}

class HaimeviskaTreeDecorator : TreeDecorator() {
    override fun getType() = HaimeviskaTreeDecoratorCard.type
    override fun generate(generator: Generator) {
        generator.logPositions.forEach { blockPos ->
            if (!generator.world.testBlockState(blockPos) { it == HaimeviskaBlockCard.LOG.block.defaultBlockState.with(PillarBlock.AXIS, Direction.Axis.Y) }) return@forEach // 垂直の幹のみ
            val direction = Direction.fromHorizontal(generator.random.nextInt(4))
            if (!generator.isAir(blockPos.offset(direction))) return@forEach // 正面が空気の場合のみ
            val r = generator.random.nextInt(100)
            if (r < 25) {
                generator.replace(blockPos, HaimeviskaBlockCard.DRIPPING_LOG.block.defaultBlockState.with(HorizontalFacingBlock.FACING, direction))
            } else if (r < 35) {
                generator.replace(blockPos, HaimeviskaBlockCard.HOLLOW_LOG.block.defaultBlockState.with(HorizontalFacingBlock.FACING, direction))
            }
        }
    }
}
