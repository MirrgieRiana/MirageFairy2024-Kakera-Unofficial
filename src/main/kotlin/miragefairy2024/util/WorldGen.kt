package miragefairy2024.util

import miragefairy2024.MirageFairy2024DataGenerator
import net.minecraft.registry.Registerable
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.util.Identifier
import net.minecraft.world.gen.feature.ConfiguredFeature
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.FeatureConfig
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.placementmodifier.PlacementModifier

infix fun <C : FeatureConfig, F : Feature<C>> F.with(config: C): ConfiguredFeature<C, F> = ConfiguredFeature(this, config)
infix fun RegistryEntry<ConfiguredFeature<*, *>>.with(placementModifiers: List<PlacementModifier>) = PlacedFeature(this, placementModifiers)


// Init

fun <T> registerDynamicGeneration(registryKey: RegistryKey<out Registry<T>>, identifier: Identifier, creator: (Registerable<T>) -> T): RegistryKey<T> {
    val key = RegistryKey.of(registryKey, identifier)
    registerDynamicGeneration(registryKey, key, creator)
    return key
}

fun <T> registerDynamicGeneration(registryKey: RegistryKey<out Registry<T>>, key: RegistryKey<T>, creator: (Registerable<T>) -> T) {
    MirageFairy2024DataGenerator.onBuildRegistry {
        it.addRegistry(registryKey) { context ->
            context.register(key, creator(context))
        }
    }
    MirageFairy2024DataGenerator.dynamicGenerationRegistries += registryKey
}
