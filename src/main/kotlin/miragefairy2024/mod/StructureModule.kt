package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.magicplant.contents.magicplants.DiamondLuminariaCard
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.RuleStructureProcessor
import miragefairy2024.util.SinglePoolElement
import miragefairy2024.util.StructurePool
import miragefairy2024.util.StructureProcessorList
import miragefairy2024.util.get
import miragefairy2024.util.register
import miragefairy2024.util.registerChestLootTableGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.times
import net.minecraft.block.Blocks
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.Items
import net.minecraft.loot.function.EnchantRandomlyLootFunction
import net.minecraft.loot.function.SetCountLootFunction
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.entry.RegistryEntryList
import net.minecraft.structure.StructureSet
import net.minecraft.structure.pool.StructurePool
import net.minecraft.structure.pool.StructurePoolBasedGenerator
import net.minecraft.structure.pool.StructurePools
import net.minecraft.structure.processor.StructureProcessorRule
import net.minecraft.structure.rule.AlwaysTrueRuleTest
import net.minecraft.structure.rule.BlockMatchRuleTest
import net.minecraft.structure.rule.RandomBlockMatchRuleTest
import net.minecraft.util.Identifier
import net.minecraft.util.collection.Pool
import net.minecraft.util.dynamic.Codecs
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.StructureSpawns
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.biome.SpawnSettings.SpawnEntry
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.HeightContext
import net.minecraft.world.gen.StructureTerrainAdaptation
import net.minecraft.world.gen.YOffset
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement
import net.minecraft.world.gen.chunk.placement.SpreadType
import net.minecraft.world.gen.heightprovider.HeightProvider
import net.minecraft.world.gen.heightprovider.UniformHeightProvider
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureType
import java.util.Optional

context(ModContext)
fun initStructureModule() {
    initUnlimitedJigsaw()
    initDripstoneCavesRuin()
}


context(ModContext)
fun initUnlimitedJigsaw() {
    UnlimitedJigsawCard.let { card ->
        card.structureType.register(Registries.STRUCTURE_TYPE, card.identifier)
    }
}

object UnlimitedJigsawCard {
    val identifier = MirageFairy2024.identifier("unlimited_jigsaw")
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


context(ModContext)
fun initDripstoneCavesRuin() {
    val identifier = MirageFairy2024.identifier("dripstone_caves_ruin")

    registerChestLootTableGeneration(identifier * "/chest_books") {
        LootTable(
            LootPool(
                ItemLootPoolEntry(Items.BOOK).weight(10).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(Items.BOOK).weight(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 10.0F))),
            ) {
                rolls(UniformLootNumberProvider.create(5.0F, 15.0F))
            },
        )
    }

    val mainElement = identifier * "/main"
    val roadCrossElement = identifier * "/road_cross"
    val roadEndElement = identifier * "/road_end"
    val roadRoomsElement = identifier * "/road_rooms"
    val roadStairsElement = identifier * "/road_stairs"
    val roadStraightElement = identifier * "/road_straight"
    val roadStraight2Element = identifier * "/road_straight2"
    val roadStraight3Element = identifier * "/road_straight3"
    val roomConferenceElement = identifier * "/room_conference"
    val roomConference2Element = identifier * "/room_conference2"
    val roomEmptyElement = identifier * "/room_empty"
    val roomEndElement = identifier * "/room_end"
    val roomFarmElement = identifier * "/room_farm"
    val roomFarm2Element = identifier * "/room_farm2"
    val roomLaboratoryElement = identifier * "/room_laboratory"
    val roomLibraryElement = identifier * "/room_library"
    val roomPrisonElement = identifier * "/room_prison"
    val roomPrison2Element = identifier * "/room_prison2"
    val roomPrison3Element = identifier * "/room_prison3"
    val roomResidenceElement = identifier * "/room_residence"
    val roomSpawnerElement = identifier * "/room_spawner"
    val stairsBottomElement = identifier * "/stairs_bottom"
    val stairsMiddleElement = identifier * "/stairs_middle"
    val stairsTopElement = identifier * "/stairs_top"

    val processorListKey = registerDynamicGeneration(RegistryKeys.PROCESSOR_LIST, identifier) {
        StructureProcessorList(
            RuleStructureProcessor(
                StructureProcessorRule(RandomBlockMatchRuleTest(Blocks.POLISHED_GRANITE, 0.1F), AlwaysTrueRuleTest.INSTANCE, Blocks.GRANITE.defaultState),
                StructureProcessorRule(RandomBlockMatchRuleTest(Blocks.LANTERN, 0.8F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.defaultState),
                StructureProcessorRule(RandomBlockMatchRuleTest(Blocks.REDSTONE_TORCH, 0.05F), AlwaysTrueRuleTest.INSTANCE, DiamondLuminariaCard.block.withAge(3)),
                StructureProcessorRule(BlockMatchRuleTest(Blocks.REDSTONE_TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.defaultState),
            ),
            RuleStructureProcessor(
                StructureProcessorRule(AlwaysTrueRuleTest.INSTANCE, BlockMatchRuleTest(Blocks.AIR), Blocks.AIR.defaultState),
                StructureProcessorRule(AlwaysTrueRuleTest.INSTANCE, BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.defaultState),
                StructureProcessorRule(AlwaysTrueRuleTest.INSTANCE, BlockMatchRuleTest(Blocks.LAVA), Blocks.LAVA.defaultState),
            ),
        )
    }

    val mainTemplatePoolKey = registerDynamicGeneration(RegistryKeys.TEMPLATE_POOL, identifier * "/main") {
        StructurePool(
            StructurePools.EMPTY,
            SinglePoolElement(mainElement, processorListKey, StructurePool.Projection.RIGID) to 1,
        )
    }

    val roadEndTemplatePoolKey = registerDynamicGeneration(RegistryKeys.TEMPLATE_POOL, identifier * "/road_end") {
        StructurePool(
            StructurePools.EMPTY,
            SinglePoolElement(roadEndElement, processorListKey, StructurePool.Projection.RIGID) to 1,
        )
    }
    val roadTemplatePoolKey = registerDynamicGeneration(RegistryKeys.TEMPLATE_POOL, identifier * "/road") {
        StructurePool(
            roadEndTemplatePoolKey,
            SinglePoolElement(roadStraightElement, processorListKey, StructurePool.Projection.RIGID) to 50,
            SinglePoolElement(roadStraight2Element, processorListKey, StructurePool.Projection.RIGID) to 10,
            SinglePoolElement(roadStraight3Element, processorListKey, StructurePool.Projection.RIGID) to 5,
            SinglePoolElement(roadRoomsElement, processorListKey, StructurePool.Projection.RIGID) to 40,
            SinglePoolElement(roadCrossElement, processorListKey, StructurePool.Projection.RIGID) to 1,
            SinglePoolElement(roadStairsElement, processorListKey, StructurePool.Projection.RIGID) to 3,
            SinglePoolElement(roadEndElement, processorListKey, StructurePool.Projection.RIGID) to 5,
        )
    }
    val stairsTemplatePoolKey = registerDynamicGeneration(RegistryKeys.TEMPLATE_POOL, identifier * "/stairs") {
        StructurePool(
            StructurePools.EMPTY,
            SinglePoolElement(stairsTopElement, processorListKey, StructurePool.Projection.RIGID) to 1,
            SinglePoolElement(stairsMiddleElement, processorListKey, StructurePool.Projection.RIGID) to 2,
            SinglePoolElement(stairsBottomElement, processorListKey, StructurePool.Projection.RIGID) to 1,
        )
    }
    val roomEndTemplatePoolKey = registerDynamicGeneration(RegistryKeys.TEMPLATE_POOL, identifier * "/room_end") {
        StructurePool(
            StructurePools.EMPTY,
            SinglePoolElement(roomEndElement, processorListKey, StructurePool.Projection.RIGID) to 1,
        )
    }
    val roomTemplatePoolKey = registerDynamicGeneration(RegistryKeys.TEMPLATE_POOL, identifier * "/room") {
        StructurePool(
            roomEndTemplatePoolKey,
            SinglePoolElement(roomConferenceElement, processorListKey, StructurePool.Projection.RIGID) to 10,
            SinglePoolElement(roomConference2Element, processorListKey, StructurePool.Projection.RIGID) to 10,
            SinglePoolElement(roomResidenceElement, processorListKey, StructurePool.Projection.RIGID) to 20,
            SinglePoolElement(roomSpawnerElement, processorListKey, StructurePool.Projection.RIGID) to 2,
            SinglePoolElement(roomPrisonElement, processorListKey, StructurePool.Projection.RIGID) to 5,
            SinglePoolElement(roomPrison2Element, processorListKey, StructurePool.Projection.RIGID) to 10,
            SinglePoolElement(roomPrison3Element, processorListKey, StructurePool.Projection.RIGID) to 2,
            SinglePoolElement(roomLibraryElement, processorListKey, StructurePool.Projection.RIGID) to 2,
            SinglePoolElement(roomLaboratoryElement, processorListKey, StructurePool.Projection.RIGID) to 5,
            SinglePoolElement(roomFarmElement, processorListKey, StructurePool.Projection.RIGID) to 10,
            SinglePoolElement(roomFarm2Element, processorListKey, StructurePool.Projection.RIGID) to 5,
            SinglePoolElement(roomEmptyElement, processorListKey, StructurePool.Projection.RIGID) to 1,
            SinglePoolElement(roomEndElement, processorListKey, StructurePool.Projection.RIGID) to 2,
        )
    }

    val structureKey = registerDynamicGeneration(RegistryKeys.STRUCTURE, identifier) {
        UnlimitedJigsawStructure(
            config = Structure.Config(
                RegistryEntryList.of(RegistryKeys.BIOME[BiomeKeys.DRIPSTONE_CAVES]),
                mapOf(
                    SpawnGroup.MONSTER to StructureSpawns(
                        StructureSpawns.BoundingBox.PIECE,
                        Pool.of(
                            SpawnEntry(ChaosCubeCard.entityType, 10, 1, 4),
                        )
                    ),
                ),
                GenerationStep.Feature.UNDERGROUND_STRUCTURES,
                StructureTerrainAdaptation.BURY,
            ),
            startPool = RegistryKeys.TEMPLATE_POOL[mainTemplatePoolKey],
            size = 12,
            startHeight = UniformHeightProvider.create(YOffset.fixed(-40), YOffset.fixed(20)),
            useExpansionHack = false,
        )
    }

    val structureSetKey = registerDynamicGeneration(RegistryKeys.STRUCTURE_SET, identifier) {
        StructureSet(
            listOf(
                StructureSet.WeightedEntry(RegistryKeys.STRUCTURE[structureKey], 1),
            ),
            RandomSpreadStructurePlacement(12, 10, SpreadType.LINEAR, 645172983),
        )
    }

}
