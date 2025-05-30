package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.ModContext
import miragefairy2024.util.BiomeSelectorScope
import miragefairy2024.util.PlacementModifiersScope
import miragefairy2024.util.get
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.with
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import java.util.function.Predicate

class FeatureContext<C : FeatureConfiguration>(val feature: Feature<C>, val card: SimpleMagicPlantCard<*>)
class ConfiguredFeatureContext(val configuredFeatureKey: ResourceKey<ConfiguredFeature<*, *>>)

context(ModContext, SimpleMagicPlantCard<*>)
operator fun <C : FeatureConfiguration> Feature<C>.invoke(block: FeatureContext<C>. () -> Unit) {
    block(FeatureContext(this, this@SimpleMagicPlantCard))
}

context(ModContext, FeatureContext<C>)
operator fun <C : FeatureConfiguration, F : Feature<C>> ResourceKey<ConfiguredFeature<*, *>>.invoke(configurationCreator: (BlockStateProvider) -> C, block: ConfiguredFeatureContext .() -> Unit) {
    registerDynamicGeneration(this) {
        val blockStateProvider = BlockStateProvider.simple(this@FeatureContext.card.block().withAge(this@FeatureContext.card.block().maxAge))
        this@FeatureContext.feature with configurationCreator(blockStateProvider)
    }
    block(ConfiguredFeatureContext(this))
}

context(ModContext, ConfiguredFeatureContext)
operator fun ResourceKey<PlacedFeature>.invoke(placementModifierCreator: PlacementModifiersScope.() -> List<PlacementModifier>, biomePredicate: (BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>)? = null) {
    registerDynamicGeneration(this) {
        val placementModifiers = placementModifiers { placementModifierCreator() }
        Registries.CONFIGURED_FEATURE[this@ConfiguredFeatureContext.configuredFeatureKey] with placementModifiers
    }
    if (biomePredicate != null) this.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION) { biomePredicate() }
}
