package miragefairy2024.mod

import com.mojang.datafixers.util.Pair
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.TerraBlenderEvents
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.registerBiomeTagGeneration
import miragefairy2024.util.registerDynamicGeneration
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryEntryLookup
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.source.util.MultiNoiseUtil
import net.minecraft.world.gen.carver.ConfiguredCarver
import net.minecraft.world.gen.feature.PlacedFeature
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import java.util.function.Consumer

@Suppress("unused")
object BiomeCards {
    val entries = mutableListOf<BiomeCard>()
}

abstract class BiomeCard(
    path: String,
    en: String,
    ja: String,
    val regionType: RegionType,
    val weight: Int,
    val temperature: MultiNoiseUtil.ParameterRange,
    val humidity: MultiNoiseUtil.ParameterRange,
    val continentalness: MultiNoiseUtil.ParameterRange,
    val erosion: MultiNoiseUtil.ParameterRange,
    val weirdness: MultiNoiseUtil.ParameterRange,
    val depth: MultiNoiseUtil.ParameterRange,
    val offset: Float,
    vararg val tags: TagKey<Biome>,
) {
    abstract fun createBiome(placedFeatureLookup: RegistryEntryLookup<PlacedFeature>, configuredCarverLookup: RegistryEntryLookup<ConfiguredCarver<*>>): Biome
    val identifier = Identifier(MirageFairy2024.modId, path)
    val registryKey: RegistryKey<Biome> = RegistryKey.of(RegistryKeys.BIOME, identifier)
    val biomeTag: TagKey<Biome> = TagKey.of(RegistryKeys.BIOME, identifier)
    val translation = Translation({ identifier.toTranslationKey("biome") }, en, ja)
}

fun initBiomeModule() {
    ModEvents.onInitialize {
        BiomeCards.entries.forEach { card ->

            // バイオームの生成
            registerDynamicGeneration(RegistryKeys.BIOME, card.registryKey) {
                card.createBiome(it.getRegistryLookup(RegistryKeys.PLACED_FEATURE), it.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER))
            }

            // このバイオームを指定するバイオームタグの生成
            card.identifier.registerBiomeTagGeneration { card.biomeTag }

            // このバイオームをタグに登録
            card.tags.forEach { tag ->
                card.identifier.registerBiomeTagGeneration { tag }
            }

            // 翻訳生成
            card.translation.enJa()

        }
    }
    TerraBlenderEvents.onTerraBlenderInitialized {
        BiomeCards.entries.forEach { card ->

            // バイオームをTerraBlenderに登録
            Regions.register(object : Region(card.identifier, card.regionType, card.weight) {
                override fun addBiomes(registry: Registry<Biome>, mapper: Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>>) {
                    addBiome(mapper, card.temperature, card.humidity, card.continentalness, card.erosion, card.weirdness, card.depth, card.offset, card.registryKey)
                }
            })

        }
    }
}
