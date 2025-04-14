package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.PlacedItemFeature
import miragefairy2024.util.BiomeSelectorScope
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.get
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.register
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.tags.BiomeTags
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt as UniformIntProvider
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration as FeatureConfig
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext as FeatureContext
import java.util.function.Predicate

val DEBRIS_FEATURE = DebrisFeature(DebrisFeature.Config.CODEC)

enum class DebrisCard(
    path: String,
    val perChunks: Int,
    val count: IntRange,
    val itemStackGetter: () -> ItemStack,
    val biomeSelectorCreator: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>,
) {
    STICK("stick", 16, 2..6, { Items.STICK.createItemStack() }, { overworld }),
    STICK_DENSE("stick_dense", 16 / 8, 2..6, { Items.STICK.createItemStack() }, { +BiomeTags.IS_FOREST }),
    BONE("bone", 32, 2..6, { Items.BONE.createItemStack() }, { overworld }),
    STRING("string", 32, 2..6, { Items.STRING.createItemStack() }, { overworld }),
    FLINT("flint", 32, 2..6, { Items.FLINT.createItemStack() }, { overworld }),
    RAW_IRON("raw_iron", 64, 2..6, { Items.RAW_IRON.createItemStack() }, { overworld }),
    RAW_IRON_DENSE("raw_iron_dense", 64 / 2, 8..24, { Items.RAW_IRON.createItemStack() }, { +BiomeTags.IS_MOUNTAIN }),
    RAW_COPPER("raw_copper", 64, 2..6, { Items.RAW_COPPER.createItemStack() }, { overworld }),
    RAW_COPPER_DENSE("raw_copper_dense", 64 / 2, 8..24, { Items.RAW_COPPER.createItemStack() }, { +BiomeTags.IS_MOUNTAIN }),
    XARPITE("xarpite", 64, 2..6, { MaterialCard.XARPITE.item.createItemStack() }, { overworld }),
    FAIRY_SCALES("fairy_scales", 64, 2..6, { MaterialCard.FAIRY_SCALES.item.createItemStack() }, { overworld }),
    FAIRY_SCALES_DENSE("fairy_scales_dense", 64 / 2, 8..24, { MaterialCard.FAIRY_SCALES.item.createItemStack() }, { +FAIRY_BIOME_TAG }),
    ;

    val identifier = MirageFairy2024.identifier("${path}_debris")
    val configuredFeatureKey = RegistryKeys.CONFIGURED_FEATURE with identifier
    val placedFeatureKey = RegistryKeys.PLACED_FEATURE with identifier
}

// TODO rei
context(ModContext)
fun initDebrisModule() {

    DEBRIS_FEATURE.register(Registries.FEATURE, MirageFairy2024.identifier("debris"))

    DebrisCard.entries.forEach { card ->
        registerDynamicGeneration(card.configuredFeatureKey) {
            DEBRIS_FEATURE with DebrisFeature.Config(UniformIntProvider.create(card.count.first, card.count.last), card.itemStackGetter())
        }
        registerDynamicGeneration(card.placedFeatureKey) {
            val placementModifiers = placementModifiers { per(card.perChunks) + flower }
            RegistryKeys.CONFIGURED_FEATURE[card.configuredFeatureKey] with placementModifiers
        }
        card.placedFeatureKey.registerFeature(GenerationStep.Decoration.VEGETAL_DECORATION, card.biomeSelectorCreator)
    }

}

class DebrisFeature(codec: Codec<Config>) : PlacedItemFeature<DebrisFeature.Config>(codec) {
    class Config(val count: IntProvider, val itemStack: ItemStack) : FeatureConfig {
        companion object {
            val CODEC: Codec<Config> = RecordCodecBuilder.create { instance ->
                instance.group(
                    IntProvider.createValidatingCodec(1, 256).fieldOf("count").forGetter { it.count },
                    ItemStack.CODEC.fieldOf("item").forGetter { it.itemStack },
                ).apply(instance, ::Config)
            }
        }
    }

    override fun getCount(context: FeatureContext<Config>) = context.config.count.get(context.random)
    override fun createItemStack(context: FeatureContext<Config>) = context.config.itemStack.copy()
}
