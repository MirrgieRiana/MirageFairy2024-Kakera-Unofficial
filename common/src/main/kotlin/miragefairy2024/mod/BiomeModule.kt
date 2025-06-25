package miragefairy2024.mod

import com.mojang.datafixers.util.Pair
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.haimeviska.HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.haimeviska.HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.magicplant.contents.magicplants.PhantomFlowerCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.registerBiomeTagGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BiomeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import terrablender.api.ParameterUtils
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import terrablender.api.SurfaceRuleManager
import java.util.function.Consumer
import net.minecraft.core.HolderGetter as RegistryEntryLookup
import net.minecraft.data.worldgen.BiomeDefaultFeatures as DefaultBiomeFeatures
import net.minecraft.data.worldgen.placement.AquaticPlacements as OceanPlacedFeatures
import net.minecraft.data.worldgen.placement.VegetationPlacements as VegetationPlacedFeatures
import net.minecraft.world.entity.MobCategory as SpawnGroup
import net.minecraft.world.level.biome.BiomeGenerationSettings as GenerationSettings
import net.minecraft.world.level.biome.BiomeSpecialEffects as BiomeEffects
import net.minecraft.world.level.biome.Climate as MultiNoiseUtil
import net.minecraft.world.level.biome.MobSpawnSettings as SpawnSettings
import net.minecraft.world.level.levelgen.Noises as NoiseParametersKeys
import net.minecraft.world.level.levelgen.SurfaceRules as MaterialRules
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver as ConfiguredCarver

val FAIRY_BIOME_TAG: TagKey<Biome> = TagKey.create(Registries.BIOME, MirageFairy2024.identifier("fairy"))

@Suppress("unused")
object BiomeCards {
    val entries = mutableListOf<BiomeCard>()

    val FAIRY_FOREST = FairyForestBiomeCard.also { entries += it }
    val DEEP_FAIRY_FOREST = DeepFairyForestBiomeCard.also { entries += it }
}

abstract class BiomeCard(
    path: String,
    en: String,
    ja: String,
    val regionType: RegionType,
    val weight: Int,
    val temperature: MultiNoiseUtil.Parameter,
    val humidity: MultiNoiseUtil.Parameter,
    val continentalness: MultiNoiseUtil.Parameter,
    val erosion: MultiNoiseUtil.Parameter,
    val weirdness: MultiNoiseUtil.Parameter,
    val depth: MultiNoiseUtil.Parameter,
    val offset: Float,
    vararg val tags: TagKey<Biome>,
) {
    abstract fun createBiome(placedFeatureLookup: RegistryEntryLookup<PlacedFeature>, configuredCarverLookup: RegistryEntryLookup<ConfiguredCarver<*>>): Biome

    context(ModContext)
    open fun init() = Unit

    val identifier = MirageFairy2024.identifier(path)
    val registryKey = Registries.BIOME with identifier
    val translation = Translation({ identifier.toLanguageKey("biome") }, en, ja)
}

context(ModContext)
fun initBiomeModule() {
    BiomeCards.entries.forEach { card ->

        // バイオームの生成
        registerDynamicGeneration(card.registryKey) {
            card.createBiome(lookup(Registries.PLACED_FEATURE), lookup(Registries.CONFIGURED_CARVER))
        }

        // このバイオームをタグに登録
        card.tags.forEach { tag ->
            card.identifier.registerBiomeTagGeneration { tag }
        }

        // 翻訳生成
        card.translation.enJa()

        card.init()
    }
    ModEvents.onTerraBlenderInitialized {
        BiomeCards.entries.forEach { card ->

            // バイオームをTerraBlenderに登録
            Regions.register(object : Region(card.identifier, card.regionType, card.weight) {
                override fun addBiomes(registry: Registry<Biome>, mapper: Consumer<Pair<MultiNoiseUtil.ParameterPoint, ResourceKey<Biome>>>) {
                    addBiome(mapper, card.temperature, card.humidity, card.continentalness, card.erosion, card.weirdness, card.depth, card.offset, card.registryKey)
                }
            })

        }
    }
}


object FairyForestBiomeCard : BiomeCard(
    "fairy_forest", "Fairy Forest", "妖精の森",
    RegionType.OVERWORLD, 1,
    ParameterUtils.Temperature.span(ParameterUtils.Temperature.COOL, ParameterUtils.Temperature.COOL),
    ParameterUtils.Humidity.span(ParameterUtils.Humidity.WET, ParameterUtils.Humidity.WET),
    ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.FAR_INLAND, ParameterUtils.Continentalness.FAR_INLAND),
    ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_1, ParameterUtils.Erosion.EROSION_1),
    ParameterUtils.Weirdness.span(ParameterUtils.Weirdness.MID_SLICE_VARIANT_DESCENDING, ParameterUtils.Weirdness.MID_SLICE_VARIANT_DESCENDING),
    ParameterUtils.Depth.span(ParameterUtils.Depth.SURFACE, ParameterUtils.Depth.SURFACE),
    0.95F,
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, ConventionalBiomeTags.IS_FLORAL, FAIRY_BIOME_TAG,
) {
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { PhantomFlowerCard.item().createItemStack() }, // TODO もっと相応しいアイコンに変える
        name = EnJa("Fairylands", "世界のそこかしこにあるおとぎの国"),
        description = EnJa("Travel the overworld and discover the Fairy Forest", "地上を旅して妖精の森を探す"),
        criterion = AdvancementCard.visit(registryKey),
        type = AdvancementCardType.TOAST_ONLY,
    )

    override fun createBiome(placedFeatureLookup: RegistryEntryLookup<PlacedFeature>, configuredCarverLookup: RegistryEntryLookup<ConfiguredCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.4F)
            .downfall(0.6F)
            .specialEffects(
                BiomeEffects.Builder()
                    .waterColor(0xF3D9FF)
                    .waterFogColor(0xF3D9FF)
                    .fogColor(0xD3C9FF)
                    .skyColor(0xA0A9FF)
                    .grassColorOverride(0x82FFBF)
                    .foliageColorOverride(0xCDAFFF)
                    .build()
            )
            .mobSpawnSettings(SpawnSettings.Builder().also { spawnSettings ->

                DefaultBiomeFeatures.caveSpawns(spawnSettings)

                spawnSettings.addSpawn(SpawnGroup.CREATURE, SpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
                spawnSettings.addSpawn(SpawnGroup.CREATURE, SpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4))

                spawnSettings.addSpawn(SpawnGroup.MONSTER, SpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4))

                // River Mobs
                spawnSettings.addSpawn(SpawnGroup.WATER_CREATURE, SpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4))
                spawnSettings.addSpawn(SpawnGroup.WATER_AMBIENT, SpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5))

            }.build())
            .generationSettings(GenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup).also { lookupBackedBuilder ->

                // BasicFeatures
                DefaultBiomeFeatures.addDefaultCarversAndLakes(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultCrystalFormations(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultMonsterRoom(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultUndergroundVariety(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultSprings(lookupBackedBuilder)
                DefaultBiomeFeatures.addSurfaceFreezing(lookupBackedBuilder)

                DefaultBiomeFeatures.addFerns(lookupBackedBuilder)

                DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY)
                DefaultBiomeFeatures.addOtherBirchTrees(lookupBackedBuilder)

                DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder)
                DefaultBiomeFeatures.addMeadowVegetation(lookupBackedBuilder)
                DefaultBiomeFeatures.addTaigaGrass(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultExtraVegetation(lookupBackedBuilder)
                DefaultBiomeFeatures.addCommonBerryBushes(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_RIVER)

            }.build()).build()
    }

    context(ModContext)
    override fun init() {
        advancement.init()
    }
}

object DeepFairyForestBiomeCard : BiomeCard(
    "deep_fairy_forest", "Deep Fairy Forest", "妖精の樹海",
    RegionType.OVERWORLD, 1,
    ParameterUtils.Temperature.span(ParameterUtils.Temperature.COOL, ParameterUtils.Temperature.COOL),
    ParameterUtils.Humidity.span(ParameterUtils.Humidity.WET, ParameterUtils.Humidity.WET),
    ParameterUtils.Continentalness.span(ParameterUtils.Continentalness.FAR_INLAND, ParameterUtils.Continentalness.FAR_INLAND),
    ParameterUtils.Erosion.span(ParameterUtils.Erosion.EROSION_0, ParameterUtils.Erosion.EROSION_0),
    ParameterUtils.Weirdness.span(ParameterUtils.Weirdness.MID_SLICE_VARIANT_DESCENDING, ParameterUtils.Weirdness.MID_SLICE_VARIANT_DESCENDING),
    ParameterUtils.Depth.span(ParameterUtils.Depth.SURFACE, ParameterUtils.Depth.SURFACE),
    0.95F,
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, FAIRY_BIOME_TAG,
) {
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { FairyForestBiomeCard.advancement.await() },
        icon = { HaimeviskaBlockCard.SAPLING.item().createItemStack() },
        name = EnJa("The Forest of Memories", "記憶の森"),
        description = EnJa("Travel the overworld and discover the Deep Fairy Forest", "地上を旅して妖精の樹海を探す"),
        criterion = AdvancementCard.visit(registryKey),
        type = AdvancementCardType.TOAST_ONLY,
    )

    override fun createBiome(placedFeatureLookup: RegistryEntryLookup<PlacedFeature>, configuredCarverLookup: RegistryEntryLookup<ConfiguredCarver<*>>): Biome {
        return Biome.BiomeBuilder()
            .hasPrecipitation(true)
            .temperature(0.4F)
            .downfall(0.6F)
            .specialEffects(
                BiomeEffects.Builder()
                    .waterColor(0xD1FCFF)
                    .waterFogColor(0xD1FCFF)
                    .fogColor(0xB7C9FF)
                    .skyColor(0x87A9FF)
                    .grassColorOverride(0x31EDCD)
                    .foliageColorOverride(0xB2A8FF)
                    .build()
            )
            .mobSpawnSettings(SpawnSettings.Builder().also { spawnSettings ->

                DefaultBiomeFeatures.commonSpawns(spawnSettings)
                spawnSettings.addSpawn(SpawnGroup.MONSTER, SpawnSettings.SpawnerData(EntityType.WITCH, 100, 1, 4))

                spawnSettings.addSpawn(SpawnGroup.CREATURE, SpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))

                // River Mobs
                spawnSettings.addSpawn(SpawnGroup.WATER_CREATURE, SpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4))
                spawnSettings.addSpawn(SpawnGroup.WATER_AMBIENT, SpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5))

            }.build())
            .generationSettings(GenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup).also { lookupBackedBuilder ->

                // BasicFeatures
                DefaultBiomeFeatures.addDefaultCarversAndLakes(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultCrystalFormations(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultMonsterRoom(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultUndergroundVariety(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultSprings(lookupBackedBuilder)
                DefaultBiomeFeatures.addSurfaceFreezing(lookupBackedBuilder)

                DefaultBiomeFeatures.addMossyStoneBlock(lookupBackedBuilder)
                DefaultBiomeFeatures.addFerns(lookupBackedBuilder)

                DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultSoftDisks(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY)

                DefaultBiomeFeatures.addTaigaGrass(lookupBackedBuilder)
                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH)
                DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder)

                lookupBackedBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_RIVER)

            }.build()).build()
    }

    context(ModContext)
    override fun init() = ModEvents.onTerraBlenderInitialized {
        val rule = MaterialRules.ifTrue(
            MaterialRules.abovePreliminarySurface(),
            MaterialRules.ifTrue(
                MaterialRules.ON_FLOOR,
                MaterialRules.ifTrue(
                    MaterialRules.waterBlockCheck(-1, 0),
                    MaterialRules.ifTrue(
                        MaterialRules.isBiome(BiomeCards.DEEP_FAIRY_FOREST.registryKey),
                        MaterialRules.sequence(
                            MaterialRules.ifTrue(
                                MaterialRules.noiseCondition(NoiseParametersKeys.SURFACE, 1.75 / 8.25, Double.MAX_VALUE),
                                MaterialRules.state(Blocks.COARSE_DIRT.defaultBlockState())
                            ),
                            MaterialRules.ifTrue(
                                MaterialRules.noiseCondition(NoiseParametersKeys.SURFACE, -0.95 / 8.25, Double.MAX_VALUE),
                                MaterialRules.state(Blocks.PODZOL.defaultBlockState())
                            ),
                        ),
                    ),
                ),
            ),
        )
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MirageFairy2024.MOD_ID, rule)
        advancement.init()
    }
}
