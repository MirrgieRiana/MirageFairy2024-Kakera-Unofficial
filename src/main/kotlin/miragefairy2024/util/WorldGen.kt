package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.block.Block
import net.minecraft.registry.Registerable
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.YOffset
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.FeatureConfig
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.CountMultilayerPlacementModifier
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier
import net.minecraft.world.gen.placementmodifier.PlacementModifier
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier
import net.minecraft.world.gen.placementmodifier.SurfaceWaterDepthFilterPlacementModifier
import java.util.function.Predicate

infix fun <C : FeatureConfig, F : Feature<C>> F.with(config: C): ConfiguredFeature<C, F> = ConfiguredFeature(this, config)
infix fun RegistryEntry<ConfiguredFeature<*, *>>.with(placementModifiers: List<PlacementModifier>) = PlacedFeature(this, placementModifiers)


// Init

context(ModContext)
fun <T> registerDynamicGeneration(registryKey: RegistryKey<out Registry<T>>, identifier: Identifier, creator: (Registerable<T>) -> T): RegistryKey<T> {
    val key = RegistryKey.of(registryKey, identifier)
    registerDynamicGeneration(key, creator)
    return key
}

context(ModContext)
fun <T> registerDynamicGeneration(key: RegistryKey<T>, creator: (Registerable<T>) -> T) {
    val registryKey = RegistryKey.ofRegistry<T>(key.registry)
    DataGenerationEvents.onBuildRegistry {
        it.addRegistry(registryKey) { context ->
            context.register(key, creator(context))
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
fun count(count: Int): List<PlacementModifier> = listOf(CountPlacementModifier.of(count))

context(PlacementModifiersScope)
fun per(chance: Int): List<PlacementModifier> = listOf(RarityFilterPlacementModifier.of(chance))

context(PlacementModifiersScope)
fun tree(saplingBlock: Block): List<PlacementModifier> = listOf(
    SquarePlacementModifier.of(),
    SurfaceWaterDepthFilterPlacementModifier.of(0),
    PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP,
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
        PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
        BiomePlacementModifier.of(),
    )

context(PlacementModifiersScope)
val netherFlower
    get(): List<PlacementModifier> = listOf(
        CountMultilayerPlacementModifier.of(1),
        BiomePlacementModifier.of(),
    )
