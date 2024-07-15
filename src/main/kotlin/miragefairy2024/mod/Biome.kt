package miragefairy2024.modules

import com.mojang.datafixers.util.Pair
import miragefairy2024.MirageFairy2024
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.BiomeTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeEffects
import net.minecraft.world.biome.GenerationSettings
import net.minecraft.world.biome.SpawnSettings
import net.minecraft.world.biome.source.util.MultiNoiseUtil
import net.minecraft.world.gen.feature.DefaultBiomeFeatures
import terrablender.api.ParameterUtils
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import java.util.function.Consumer

enum class BiomeCard(
    val path: String,
    val en: String,
    val ja: String,
) {
    FAIRY_FOREST("fairy_forest", "Fairy Forest", "妖精の森"),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val biomeTag: TagKey<Biome> = TagKey.of(Registries.BIOME_KEY, identifier)
    val biome = Biome.Builder()
        .precipitation(Biome.Precipitation.RAIN)
        .temperature(0.4F)
        .downfall(0.6F)
        .effects(
            BiomeEffects.Builder()
                .waterColor(0xF3D9FF)
                .waterFogColor(0xF3D9FF)
                .fogColor(0xF7AFFF)
                .skyColor(0xF7AFFF)
                .grassColor(0x82FFBF)
                .foliageColor(0xCDAFFF)
                .build()
        )
        .spawnSettings(SpawnSettings.Builder().also { spawnSettings ->
            DefaultBiomeFeatures.addCaveMobs(spawnSettings)
            spawnSettings.spawn(SpawnGroup.MONSTER, SpawnSettings.SpawnEntry(EntityType.ENDERMAN, 10, 1, 4))
            spawnSettings.spawn(SpawnGroup.CREATURE, SpawnSettings.SpawnEntry(EntityType.RABBIT, 4, 2, 3))
            spawnSettings.spawn(SpawnGroup.CREATURE, SpawnSettings.SpawnEntry(EntityType.FOX, 8, 2, 4))
        }.build())
        .generationSettings(GenerationSettings.Builder().also { generationSettings ->
            DefaultBiomeFeatures.addLandCarvers(generationSettings)
            DefaultBiomeFeatures.addAmethystGeodes(generationSettings)
            DefaultBiomeFeatures.addDungeons(generationSettings)
            DefaultBiomeFeatures.addMineables(generationSettings)
            DefaultBiomeFeatures.addSprings(generationSettings)
            DefaultBiomeFeatures.addFrozenTopLayer(generationSettings)
            DefaultBiomeFeatures.addLargeFerns(generationSettings)
            DefaultBiomeFeatures.addDefaultOres(generationSettings)
            DefaultBiomeFeatures.addDefaultDisks(generationSettings)

            //DefaultBiomeFeatures.addTaigaTrees(generationSettings)
            DefaultBiomeFeatures.addForestTrees(generationSettings)

            //DefaultBiomeFeatures.addDefaultFlowers(generationSettings)
            DefaultBiomeFeatures.addDefaultFlowers(generationSettings)
            DefaultBiomeFeatures.addMeadowFlowers(generationSettings)

            DefaultBiomeFeatures.addTaigaGrass(generationSettings)
            DefaultBiomeFeatures.addDefaultVegetation(generationSettings)
            DefaultBiomeFeatures.addSweetBerryBushes(generationSettings)

        }.build()).build()
}

fun initBiomeModule() {

    BiomeCard.entries.forEach { card ->
        onGenerateBiome {
            it.map[card.identifier] = card.biome
        }
        generateBiomeTag(card.biomeTag, card.identifier)
        enJa({ "biome.${card.identifier.toTranslationKey()}" }, card.en, card.ja)
    }

    generateBiomeTag(BiomeTags.IS_FOREST, BiomeCard.FAIRY_FOREST.identifier)

    onTerraBlenderInitialized {
        Regions.register(object : Region(Identifier(MirageFairy2024.modId, "fairy_forest"), RegionType.OVERWORLD, 1) {
            override fun addBiomes(registry: Registry<Biome>, mapper: Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>>) {
                addBiome(
                    mapper,
                    ParameterUtils.Temperature.span(ParameterUtils.Temperature.ICY, ParameterUtils.Temperature.NEUTRAL),
                    ParameterUtils.Humidity.span(ParameterUtils.Humidity.NEUTRAL, ParameterUtils.Humidity.HUMID),
                    ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.NEAR_INLAND, ParameterUtils.Continentalness.FAR_INLAND),
                    ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_2, ParameterUtils.Erosion.EROSION_5),
                    ParameterUtils.Weirdness.span(ParameterUtils.Weirdness.MID_SLICE_VARIANT_ASCENDING, ParameterUtils.Weirdness.MID_SLICE_VARIANT_DESCENDING),
                    ParameterUtils.Depth.span(ParameterUtils.Depth.SURFACE, ParameterUtils.Depth.SURFACE),
                    0.0F,
                    RegistryKey.of(BuiltinRegistries.BIOME.key, BiomeCard.FAIRY_FOREST.identifier),
                )
            }
        })
    }

}
