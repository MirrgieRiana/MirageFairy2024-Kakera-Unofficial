package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import mirrg.kotlin.hydrogen.floorToInt
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.world.level.block.Block
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory as SpawnGroup
import net.minecraft.data.worldgen.BootstapContext as Registerable
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey as RegistryKey
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.core.Holder as RegistryEntry
import net.minecraft.core.HolderSet as RegistryEntryList
import net.minecraft.tags.TagKey
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool as StructurePool
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor as RuleStructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule as StructureProcessorRule
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.util.random.SimpleWeightedRandomList as DataPool
import net.minecraft.core.Direction
import net.minecraft.util.valueproviders.ConstantInt as ConstantIntProvider
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.WeightedListInt as WeightedListIntProvider
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.VerticalAnchor as YOffset
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration as FeatureConfig
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.world.level.levelgen.placement.BiomeFilter as BiomePlacementModifier
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement as CountMultilayerPlacementModifier
import net.minecraft.world.level.levelgen.placement.CountPlacement as CountPlacementModifier
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement as EnvironmentScanPlacementModifier
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement as HeightRangePlacementModifier
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement as RandomOffsetPlacementModifier
import net.minecraft.world.level.levelgen.placement.RarityFilter as RarityFilterPlacementModifier
import net.minecraft.world.level.levelgen.placement.InSquarePlacement as SquarePlacementModifier
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter as SurfaceWaterDepthFilterPlacementModifier
import java.util.function.Predicate
import kotlin.math.roundToInt

infix fun <C : FeatureConfig, F : Feature<C>> F.with(config: C): ConfiguredFeature<C, F> = ConfiguredFeature(this, config)
infix fun RegistryEntry<ConfiguredFeature<*, *>>.with(placementModifiers: List<PlacementModifier>) = PlacedFeature(this, placementModifiers)


// Init

class DynamicGenerationScope<T>(val context: Registerable<T>)

context(DynamicGenerationScope<*>)
operator fun <T> RegistryKey<Registry<T>>.get(key: RegistryKey<T>): RegistryEntry<T> {
    return this@DynamicGenerationScope.context.getRegistryLookup(this).getOrThrow(key)
}

context(DynamicGenerationScope<*>)
operator fun <T> RegistryKey<Registry<T>>.get(key: TagKey<T>): RegistryEntryList.Named<T> {
    return this@DynamicGenerationScope.context.getRegistryLookup(this).getOrThrow(key)
}

context(ModContext)
fun <T> registerDynamicGeneration(registryKey: RegistryKey<out Registry<T>>, identifier: Identifier, creator: context(DynamicGenerationScope<T>) () -> T): RegistryKey<T> {
    val key = registryKey with identifier
    registerDynamicGeneration(key, creator)
    return key
}

context(ModContext)
fun <T> registerDynamicGeneration(key: RegistryKey<T>, creator: context(DynamicGenerationScope<T>) () -> T) {
    val registryKey = RegistryKey.createRegistryKey<T>(key.registry)
    DataGenerationEvents.onBuildRegistry {
        it.addRegistry(registryKey) { context ->
            context.register(key, creator(DynamicGenerationScope(context)))
        }
    }
    DataGenerationEvents.onInitializeDataGenerator {
        MirageFairy2024DataGenerator.dynamicGenerationRegistries += registryKey
    }
}

context(ModContext)
fun RegistryKey<PlacedFeature>.registerFeature(step: GenerationStep.Feature, biomeSelectorCreator: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>) = ModEvents.onInitialize {
    BiomeModifications.addFeature(biomeSelectorCreator(BiomeSelectorScope), step, this)
}

context(ModContext)
fun EntityType<*>.registerSpawn(spawnGroup: SpawnGroup, weight: Int, minGroupSize: Int, maxGroupSize: Int, biomeSelectorCreator: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>) = ModEvents.onInitialize {
    BiomeModifications.addSpawn(biomeSelectorCreator(BiomeSelectorScope), spawnGroup, this, weight, minGroupSize, maxGroupSize)
}


// BiomeSelectorScope

object BiomeSelectorScope

context(BiomeSelectorScope) val all: Predicate<BiomeSelectionContext> get() = BiomeSelectors.all()
context(BiomeSelectorScope) val overworld: Predicate<BiomeSelectionContext> get() = BiomeSelectors.foundInOverworld()
context(BiomeSelectorScope) val nether: Predicate<BiomeSelectionContext> get() = BiomeSelectors.foundInTheNether()
context(BiomeSelectorScope) val end: Predicate<BiomeSelectionContext> get() = BiomeSelectors.foundInTheEnd()
context(BiomeSelectorScope) operator fun RegistryKey<Biome>.unaryPlus(): Predicate<BiomeSelectionContext> = BiomeSelectors.includeByKey(this)
context(BiomeSelectorScope) operator fun TagKey<Biome>.unaryPlus(): Predicate<BiomeSelectionContext> = BiomeSelectors.tag(this)
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.not(): Predicate<BiomeSelectionContext> = this.negate()
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.times(other: Predicate<BiomeSelectionContext>): Predicate<BiomeSelectionContext> = this.and(other)
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.plus(other: Predicate<BiomeSelectionContext>): Predicate<BiomeSelectionContext> = this.or(other)


// PlacementModifier

object PlacementModifiersScope

fun placementModifiers(block: PlacementModifiersScope.() -> List<PlacementModifier>) = block(PlacementModifiersScope)

context(PlacementModifiersScope)
fun randomIntCount(count: Double): List<PlacementModifier> {
    val min = count.floorToInt()
    val rate = ((count - min.toDouble()) * 100).roundToInt()
    if (rate == 0) return count(min)
    if (rate == 100) return count(min + 1)
    val dataPool = DataPool.builder<IntProvider>()
        .add(ConstantIntProvider.create(min), 100 - rate)
        .add(ConstantIntProvider.create(min + 1), rate)
        .build()
    val intProvider = WeightedListIntProvider(dataPool)
    return listOf(CountPlacementModifier.of(intProvider))
}

context(PlacementModifiersScope)
fun count(count: Int): List<PlacementModifier> = listOf(CountPlacementModifier.of(count))

context(PlacementModifiersScope)
fun per(chance: Int): List<PlacementModifier> = listOf(RarityFilterPlacementModifier.of(chance))

context(PlacementModifiersScope)
fun tree(saplingBlock: Block): List<PlacementModifier> = listOf(
    SquarePlacementModifier.of(),
    SurfaceWaterDepthFilterPlacementModifier.of(0),
    PlacedFeatures.HEIGHTMAP_OCEAN_FLOOR,
    BiomePlacementModifier.of(),
    PlacedFeatures.wouldSurvive(saplingBlock),
)

context(PlacementModifiersScope)
fun uniformOre(minOffset: Int, maxOffset: Int): List<PlacementModifier> = listOf(
    SquarePlacementModifier.of(),
    HeightRangePlacementModifier.uniform(YOffset.fixed(minOffset), YOffset.fixed(maxOffset)),
    BiomePlacementModifier.of(),
)

context(PlacementModifiersScope)
val flower: List<PlacementModifier>
    get() = listOf(
        SquarePlacementModifier.of(),
        PlacedFeatures.HEIGHTMAP,
        BiomePlacementModifier.of(),
    )

context(PlacementModifiersScope)
val netherFlower
    get(): List<PlacementModifier> = listOf(
        CountMultilayerPlacementModifier.of(1),
        BiomePlacementModifier.of(),
    )

context(PlacementModifiersScope)
val undergroundFlower: List<PlacementModifier>
    get() = listOf(
        SquarePlacementModifier.of(),
        PlacedFeatures.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
        EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12),
        RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(1)),
        BiomePlacementModifier.of(),
    )


// Structure

context(DynamicGenerationScope<*>)
fun RuleStructureProcessor(vararg rules: StructureProcessorRule): RuleStructureProcessor {
    return RuleStructureProcessor(rules.toList())
}

context(DynamicGenerationScope<*>)
fun StructureProcessorList(vararg processors: StructureProcessor): StructureProcessorList {
    return StructureProcessorList(processors.toList())
}

context(DynamicGenerationScope<*>)
fun SinglePoolElement(location: Identifier, processorsKey: RegistryKey<StructureProcessorList>, projection: StructurePool.Projection): StructurePoolElement {
    return StructurePoolElement.ofProcessedSingle(location.string, RegistryKeys.PROCESSOR_LIST[processorsKey]).apply(projection)
}

context(DynamicGenerationScope<*>)
fun StructurePool(fallbackKey: RegistryKey<StructurePool>, vararg elements: Pair<StructurePoolElement, Int>): StructurePool {
    return StructurePool(RegistryKeys.TEMPLATE_POOL[fallbackKey], elements.map { com.mojang.datafixers.util.Pair.of(it.first, it.second) })
}
