package miragefairy2024.mod

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.TerraBlenderEvents
import miragefairy2024.mod.haimeviska.HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.haimeviska.HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerConfiguration
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.registerBiomeTagGeneration
import miragefairy2024.util.registerChestLootTableGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.Items
import net.minecraft.loot.function.EnchantRandomlyLootFunction
import net.minecraft.loot.function.SetCountLootFunction
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryEntryLookup
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.BiomeTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.structure.pool.StructurePool
import net.minecraft.structure.pool.StructurePoolBasedGenerator
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.Codecs
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeEffects
import net.minecraft.world.biome.GenerationSettings
import net.minecraft.world.biome.SpawnSettings
import net.minecraft.world.biome.SpawnSettings.SpawnEntry
import net.minecraft.world.biome.source.util.MultiNoiseUtil
import net.minecraft.world.biome.source.util.MultiNoiseUtil.ParameterRange
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.HeightContext
import net.minecraft.world.gen.StructureTerrainAdaptation
import net.minecraft.world.gen.carver.ConfiguredCarver
import net.minecraft.world.gen.feature.DefaultBiomeFeatures
import net.minecraft.world.gen.feature.OceanPlacedFeatures
import net.minecraft.world.gen.feature.PlacedFeature
import net.minecraft.world.gen.feature.VegetationPlacedFeatures
import net.minecraft.world.gen.heightprovider.HeightProvider
import net.minecraft.world.gen.noise.NoiseParametersKeys
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureType
import net.minecraft.world.gen.surfacebuilder.MaterialRules
import terrablender.api.ParameterUtils
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import terrablender.api.SurfaceRuleManager
import java.util.Optional
import java.util.function.Consumer

val FAIRY_BIOME_TAG: TagKey<Biome> = TagKey.of(RegistryKeys.BIOME, MirageFairy2024.identifier("fairy"))

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
    val temperature: ParameterRange,
    val humidity: ParameterRange,
    val continentalness: ParameterRange,
    val erosion: ParameterRange,
    val weirdness: ParameterRange,
    val depth: ParameterRange,
    val offset: Float,
    vararg val tags: TagKey<Biome>,
) {
    abstract fun createBiome(placedFeatureLookup: RegistryEntryLookup<PlacedFeature>, configuredCarverLookup: RegistryEntryLookup<ConfiguredCarver<*>>): Biome
    context(ModContext)
    open fun init() = Unit

    val identifier = MirageFairy2024.identifier(path)
    val registryKey = RegistryKeys.BIOME with identifier
    val translation = Translation({ identifier.toTranslationKey("biome") }, en, ja)
}

context(ModContext)
fun initBiomeModule() {
    BiomeCards.entries.forEach { card ->

        // バイオームの生成
        registerDynamicGeneration(card.registryKey) {
            card.createBiome(it.getRegistryLookup(RegistryKeys.PLACED_FEATURE), it.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER))
        }

        // このバイオームをタグに登録
        card.tags.forEach { tag ->
            card.identifier.registerBiomeTagGeneration { tag }
        }

        // 翻訳生成
        card.translation.enJa()

        card.init()
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

    ModEvents.onRegistration {
        Registry.register(Registries.STRUCTURE_TYPE, MirageFairy2024.identifier("unlimited_jigsaw"), UnlimitedJigsawCard.structureType)
    }

    registerChestLootTableGeneration(MirageFairy2024.identifier("dripstone_caves_ruin/chest_books")) {
        LootTable(
            LootPool(
                ItemLootPoolEntry(Items.BOOK).weight(10).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(Items.BOOK).weight(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 10.0F))),
            ) {
                rolls(UniformLootNumberProvider.create(5.0F, 15.0F))
            },
        )
    }

}

object UnlimitedJigsawCard {
    val structureType: StructureType<UnlimitedJigsawStructure> = StructureType { UnlimitedJigsawStructure.CODEC }
}

class UnlimitedJigsawStructure(
    config: Config,
    private val startPool: RegistryEntry<StructurePool>,
    private val startJigsawName: Optional<Identifier> = Optional.empty(),
    private val size: Int,
    private val startHeight: HeightProvider,
    private val useExpansionHack: Boolean,
    private val projectStartToHeightmap: Optional<Heightmap.Type> = Optional.empty(),
    private val maxDistanceFromCenter: Int = 80,
) : Structure(config) {
    companion object {
        val CODEC: Codec<UnlimitedJigsawStructure> = Codecs.validate(RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                configCodecBuilder(instance),
                StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter { it.startPool },
                Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter { it.startJigsawName },
                Codec.intRange(0, 256).fieldOf("size").forGetter { it.size },
                HeightProvider.CODEC.fieldOf("start_height").forGetter { it.startHeight },
                Codec.BOOL.fieldOf("use_expansion_hack").forGetter { it.useExpansionHack },
                Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter { it.projectStartToHeightmap },
                Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter { it.maxDistanceFromCenter },
            ).apply(instance, ::UnlimitedJigsawStructure)
        }, UnlimitedJigsawStructure::validate).codec()

        private fun validate(structure: UnlimitedJigsawStructure): DataResult<UnlimitedJigsawStructure> {
            val var10000 = when (structure.terrainAdaptation) {
                StructureTerrainAdaptation.NONE -> 0
                StructureTerrainAdaptation.BURY, StructureTerrainAdaptation.BEARD_THIN, StructureTerrainAdaptation.BEARD_BOX -> 12
                else -> throw IncompatibleClassChangeError()
            }
            return if (structure.maxDistanceFromCenter + var10000 > 128) {
                DataResult.error { "Structure size including terrain adaptation must not exceed 128" }
            } else {
                DataResult.success(structure)
            }
        }
    }

    override fun getStructurePosition(context: Context): Optional<StructurePosition> {
        val chunkPos = context.chunkPos()
        val i = startHeight.get(context.random(), HeightContext(context.chunkGenerator(), context.world()))
        val blockPos = BlockPos(chunkPos.startX, i, chunkPos.startZ)
        return StructurePoolBasedGenerator.generate(context, startPool, startJigsawName, size, blockPos, useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter)
    }

    override fun getType(): StructureType<*> = UnlimitedJigsawCard.structureType
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
    BiomeTags.IS_OVERWORLD, BiomeTags.IS_FOREST, ConventionalBiomeTags.FLORAL, FAIRY_BIOME_TAG,
) {
    override fun createBiome(placedFeatureLookup: RegistryEntryLookup<PlacedFeature>, configuredCarverLookup: RegistryEntryLookup<ConfiguredCarver<*>>): Biome {
        return Biome.Builder()
            .precipitation(true)
            .temperature(0.4F)
            .downfall(0.6F)
            .effects(
                BiomeEffects.Builder()
                    .waterColor(0xF3D9FF)
                    .waterFogColor(0xF3D9FF)
                    .fogColor(0xD3C9FF)
                    .skyColor(0xA0A9FF)
                    .grassColor(0x82FFBF)
                    .foliageColor(0xCDAFFF)
                    .build()
            )
            .spawnSettings(SpawnSettings.Builder().also { spawnSettings ->

                DefaultBiomeFeatures.addCaveMobs(spawnSettings)

                spawnSettings.spawn(SpawnGroup.CREATURE, SpawnEntry(EntityType.RABBIT, 4, 2, 3))
                spawnSettings.spawn(SpawnGroup.CREATURE, SpawnEntry(EntityType.FOX, 8, 2, 4))

                spawnSettings.spawn(SpawnGroup.MONSTER, SpawnEntry(EntityType.ENDERMAN, 10, 1, 4))

                // River Mobs
                spawnSettings.spawn(SpawnGroup.WATER_CREATURE, SpawnEntry(EntityType.SQUID, 2, 1, 4))
                spawnSettings.spawn(SpawnGroup.WATER_AMBIENT, SpawnEntry(EntityType.SALMON, 5, 1, 5))

            }.build())
            .generationSettings(GenerationSettings.LookupBackedBuilder(placedFeatureLookup, configuredCarverLookup).also { lookupBackedBuilder ->

                // BasicFeatures
                DefaultBiomeFeatures.addLandCarvers(lookupBackedBuilder)
                DefaultBiomeFeatures.addAmethystGeodes(lookupBackedBuilder)
                DefaultBiomeFeatures.addDungeons(lookupBackedBuilder)
                DefaultBiomeFeatures.addMineables(lookupBackedBuilder)
                DefaultBiomeFeatures.addSprings(lookupBackedBuilder)
                DefaultBiomeFeatures.addFrozenTopLayer(lookupBackedBuilder)

                DefaultBiomeFeatures.addLargeFerns(lookupBackedBuilder)

                DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder)

                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, HAIMEVISKA_FAIRY_FOREST_PLACED_FEATURE_KEY)
                DefaultBiomeFeatures.addForestTrees(lookupBackedBuilder)

                DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder)
                DefaultBiomeFeatures.addMeadowFlowers(lookupBackedBuilder)
                DefaultBiomeFeatures.addTaigaGrass(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder)
                DefaultBiomeFeatures.addSweetBerryBushes(lookupBackedBuilder)
                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, MirageFlowerConfiguration.MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY)

                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_RIVER)

            }.build()).build()
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
    override fun createBiome(placedFeatureLookup: RegistryEntryLookup<PlacedFeature>, configuredCarverLookup: RegistryEntryLookup<ConfiguredCarver<*>>): Biome {
        return Biome.Builder()
            .precipitation(true)
            .temperature(0.4F)
            .downfall(0.6F)
            .effects(
                BiomeEffects.Builder()
                    .waterColor(0xD1FCFF)
                    .waterFogColor(0xD1FCFF)
                    .fogColor(0xB7C9FF)
                    .skyColor(0x87A9FF)
                    .grassColor(0x31EDCD)
                    .foliageColor(0xB2A8FF)
                    .build()
            )
            .spawnSettings(SpawnSettings.Builder().also { spawnSettings ->

                DefaultBiomeFeatures.addBatsAndMonsters(spawnSettings)
                spawnSettings.spawn(SpawnGroup.MONSTER, SpawnEntry(EntityType.WITCH, 100, 1, 4))

                spawnSettings.spawn(SpawnGroup.CREATURE, SpawnEntry(EntityType.WOLF, 8, 4, 4))

                // River Mobs
                spawnSettings.spawn(SpawnGroup.WATER_CREATURE, SpawnEntry(EntityType.SQUID, 2, 1, 4))
                spawnSettings.spawn(SpawnGroup.WATER_AMBIENT, SpawnEntry(EntityType.SALMON, 5, 1, 5))

            }.build())
            .generationSettings(GenerationSettings.LookupBackedBuilder(placedFeatureLookup, configuredCarverLookup).also { lookupBackedBuilder ->

                // BasicFeatures
                DefaultBiomeFeatures.addLandCarvers(lookupBackedBuilder)
                DefaultBiomeFeatures.addAmethystGeodes(lookupBackedBuilder)
                DefaultBiomeFeatures.addDungeons(lookupBackedBuilder)
                DefaultBiomeFeatures.addMineables(lookupBackedBuilder)
                DefaultBiomeFeatures.addSprings(lookupBackedBuilder)
                DefaultBiomeFeatures.addFrozenTopLayer(lookupBackedBuilder)

                DefaultBiomeFeatures.addMossyRocks(lookupBackedBuilder)
                DefaultBiomeFeatures.addLargeFerns(lookupBackedBuilder)

                DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder)
                DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder)

                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, HAIMEVISKA_DEEP_FAIRY_FOREST_PLACED_FEATURE_KEY)

                DefaultBiomeFeatures.addTaigaGrass(lookupBackedBuilder)
                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH)
                DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder)
                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, MirageFlowerConfiguration.MIRAGE_CLUSTER_FAIRY_FOREST_PLACED_FEATURE_KEY)

                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_RIVER)

            }.build()).build()
    }

    context(ModContext)
    override fun init() = TerraBlenderEvents.onTerraBlenderInitialized {
        val rule = MaterialRules.condition(
            MaterialRules.surface(),
            MaterialRules.condition(
                MaterialRules.STONE_DEPTH_FLOOR,
                MaterialRules.condition(
                    MaterialRules.water(-1, 0),
                    MaterialRules.condition(
                        MaterialRules.biome(BiomeCards.DEEP_FAIRY_FOREST.registryKey),
                        MaterialRules.sequence(
                            MaterialRules.condition(
                                MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, 1.75 / 8.25, Double.MAX_VALUE),
                                MaterialRules.block(Blocks.COARSE_DIRT.defaultState)
                            ),
                            MaterialRules.condition(
                                MaterialRules.noiseThreshold(NoiseParametersKeys.SURFACE, -0.95 / 8.25, Double.MAX_VALUE),
                                MaterialRules.block(Blocks.PODZOL.defaultState)
                            ),
                        ),
                    ),
                ),
            ),
        )
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MirageFairy2024.MOD_ID, rule)
    }
}
