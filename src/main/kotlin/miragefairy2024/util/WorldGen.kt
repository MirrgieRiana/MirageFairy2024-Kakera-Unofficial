package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.registry.Registerable
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.FeatureConfig
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.placementmodifier.PlacementModifier
import java.util.function.Predicate

infix fun <C : FeatureConfig, F : Feature<C>> F.with(config: C): ConfiguredFeature<C, F> = ConfiguredFeature(this, config)
infix fun RegistryEntry<ConfiguredFeature<*, *>>.with(placementModifiers: List<PlacementModifier>) = PlacedFeature(this, placementModifiers)


// Init

context(ModContext)
fun <T> registerDynamicGeneration(registryKey: RegistryKey<out Registry<T>>, identifier: Identifier, creator: (Registerable<T>) -> T): RegistryKey<T> {
    val key = RegistryKey.of(registryKey, identifier)
    registerDynamicGeneration(registryKey, key, creator)
    return key
}

context(ModContext)
fun <T> registerDynamicGeneration(registryKey: RegistryKey<out Registry<T>>, key: RegistryKey<T>, creator: (Registerable<T>) -> T) {
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
context(BiomeSelectorScope) fun biome(vararg biomes: RegistryKey<Biome>): Predicate<BiomeSelectionContext> = BiomeSelectors.includeByKey(*biomes)
context(BiomeSelectorScope) fun biome(biomes: Collection<RegistryKey<Biome>>): Predicate<BiomeSelectionContext> = BiomeSelectors.includeByKey(biomes)
context(BiomeSelectorScope) fun tag(tag: TagKey<Biome>): Predicate<BiomeSelectionContext> = BiomeSelectors.tag(tag)
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.not(): Predicate<BiomeSelectionContext> = this.negate()
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.times(other: Predicate<BiomeSelectionContext>): Predicate<BiomeSelectionContext> = this.and(other)
context(BiomeSelectorScope) operator fun Predicate<BiomeSelectionContext>.plus(other: Predicate<BiomeSelectionContext>): Predicate<BiomeSelectionContext> = this.or(other)
