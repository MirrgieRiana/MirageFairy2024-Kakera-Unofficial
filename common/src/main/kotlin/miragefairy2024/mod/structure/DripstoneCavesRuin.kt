package miragefairy2024.mod.structure

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.magicplant.contents.magicplants.DiamondLuminariaCard
import miragefairy2024.mod.materials.block.BlockMaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.RuleStructureProcessor
import miragefairy2024.util.SinglePoolElement
import miragefairy2024.util.StructurePool
import miragefairy2024.util.StructureProcessorList
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.registerChestLootTableGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerStructureTagGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
import net.minecraft.core.HolderSet as RegistryEntryList
import net.minecraft.data.worldgen.Pools as StructurePools
import net.minecraft.util.random.WeightedRandomList as Pool
import net.minecraft.world.entity.MobCategory as SpawnGroup
import net.minecraft.world.level.biome.Biomes as BiomeKeys
import net.minecraft.world.level.biome.MobSpawnSettings as SpawnSettings
import net.minecraft.world.level.levelgen.VerticalAnchor as YOffset
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight as UniformHeightProvider
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride as StructureSpawns
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment as StructureTerrainAdaptation
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType as SpreadType
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool as StructurePool
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest as AlwaysTrueRuleTest
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest as BlockMatchRuleTest
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule as StructureProcessorRule
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest as RandomBlockMatchRuleTest
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction as EnchantRandomlyLootFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction as SetCountLootFunction
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator as UniformLootNumberProvider

object DripstoneCavesRuinCard {
    val identifier = MirageFairy2024.identifier("dripstone_caves_ruin")
    val key = Registries.STRUCTURE with identifier
    val translation = Translation({ identifier.toLanguageKey("structure") }, "Dripstone Cave Ruin", "鍾乳洞の遺跡")

    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { WeatheredAncientRemnantsCard.advancement.await() },
        icon = { BlockMaterialCard.CHAOS_STONE_BLOCK.item().createItemStack() },
        name = EnJa("The Ancient Future Civ", "古代の未来文明"),
        description = EnJa("Unearth a map from the Weathered Ancient Remnants and explore the Dripstone Cave Ruin", "風化した旧世代の遺構から地図を発掘し、鍾乳洞の遺跡を訪れる"),
        criterion = AdvancementCard.visit(key),
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    fun init() {

        identifier.registerStructureTagGeneration { WeatheredAncientRemnantsCard.onMapsTag }
        translation.enJa()


        registerChestLootTableGeneration(Registries.LOOT_TABLE with "chests/" * identifier * "/chest_books") { registries ->
            LootTable(
                LootPool(
                    ItemLootPoolEntry(Items.BOOK).setWeight(10).apply(EnchantRandomlyLootFunction.randomApplicableEnchantment(registries)),
                    ItemLootPoolEntry(Items.BOOK).setWeight(2).apply(SetCountLootFunction.setCount(UniformLootNumberProvider.between(1.0F, 10.0F))),
                ) {
                    setRolls(UniformLootNumberProvider.between(5.0F, 15.0F))
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
        val roadMobs = identifier * "/road_mobs"
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
        val mobElement = identifier * "/mob"
        val mobEmptyElement = identifier * "/mob_empty"

        val processorListKey = registerDynamicGeneration(Registries.PROCESSOR_LIST, identifier) {
            StructureProcessorList(
                RuleStructureProcessor(
                    StructureProcessorRule(RandomBlockMatchRuleTest(Blocks.POLISHED_GRANITE, 0.1F), AlwaysTrueRuleTest.INSTANCE, Blocks.GRANITE.defaultBlockState()),
                    StructureProcessorRule(RandomBlockMatchRuleTest(Blocks.LANTERN, 0.8F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                    StructureProcessorRule(RandomBlockMatchRuleTest(Blocks.REDSTONE_TORCH, 0.05F), AlwaysTrueRuleTest.INSTANCE, DiamondLuminariaCard.block().withAge(3)),
                    StructureProcessorRule(BlockMatchRuleTest(Blocks.REDSTONE_TORCH), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.defaultBlockState()),
                ),
                RuleStructureProcessor(
                    StructureProcessorRule(AlwaysTrueRuleTest.INSTANCE, BlockMatchRuleTest(Blocks.AIR), Blocks.AIR.defaultBlockState()),
                    StructureProcessorRule(AlwaysTrueRuleTest.INSTANCE, BlockMatchRuleTest(Blocks.WATER), Blocks.WATER.defaultBlockState()),
                    StructureProcessorRule(AlwaysTrueRuleTest.INSTANCE, BlockMatchRuleTest(Blocks.LAVA), Blocks.LAVA.defaultBlockState()),
                ),
            )
        }

        val mainTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/main") {
            StructurePool(
                StructurePools.EMPTY,
                SinglePoolElement(mainElement, processorListKey, StructurePool.Projection.RIGID) to 1,
            )
        }

        val roadEndTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/road_end") {
            StructurePool(
                StructurePools.EMPTY,
                SinglePoolElement(roadEndElement, processorListKey, StructurePool.Projection.RIGID) to 1,
            )
        }
        val roadTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/road") {
            StructurePool(
                roadEndTemplatePoolKey,
                SinglePoolElement(roadStraightElement, processorListKey, StructurePool.Projection.RIGID) to 40,
                SinglePoolElement(roadStraight2Element, processorListKey, StructurePool.Projection.RIGID) to 10,
                SinglePoolElement(roadStraight3Element, processorListKey, StructurePool.Projection.RIGID) to 5,
                SinglePoolElement(roadRoomsElement, processorListKey, StructurePool.Projection.RIGID) to 40,
                SinglePoolElement(roadCrossElement, processorListKey, StructurePool.Projection.RIGID) to 1,
                SinglePoolElement(roadStairsElement, processorListKey, StructurePool.Projection.RIGID) to 3,
                SinglePoolElement(roadEndElement, processorListKey, StructurePool.Projection.RIGID) to 5,
                SinglePoolElement(roadMobs, processorListKey, StructurePool.Projection.RIGID) to 10,
            )
        }
        val stairsTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/stairs") {
            StructurePool(
                StructurePools.EMPTY,
                SinglePoolElement(stairsTopElement, processorListKey, StructurePool.Projection.RIGID) to 1,
                SinglePoolElement(stairsMiddleElement, processorListKey, StructurePool.Projection.RIGID) to 2,
                SinglePoolElement(stairsBottomElement, processorListKey, StructurePool.Projection.RIGID) to 1,
            )
        }
        val roomEndTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/room_end") {
            StructurePool(
                StructurePools.EMPTY,
                SinglePoolElement(roomEndElement, processorListKey, StructurePool.Projection.RIGID) to 1,
            )
        }
        val roomTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/room") {
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
        val mobTemplatePoolKey = registerDynamicGeneration(Registries.TEMPLATE_POOL, identifier * "/mob") {
            StructurePool(
                StructurePools.EMPTY,
                SinglePoolElement(mobElement, processorListKey, StructurePool.Projection.RIGID) to 10,
                SinglePoolElement(mobEmptyElement, processorListKey, StructurePool.Projection.RIGID) to 3,
            )
        }

        val structureKey = registerDynamicGeneration(Registries.STRUCTURE, identifier) {
            UnlimitedJigsawStructure(
                config = Structure.StructureSettings(
                    RegistryEntryList.direct(Registries.BIOME[BiomeKeys.DRIPSTONE_CAVES]),
                    mapOf(
                        SpawnGroup.MONSTER to StructureSpawns(
                            StructureSpawns.BoundingBoxType.PIECE,
                            Pool.create(
                                SpawnSettings.SpawnerData(ChaosCubeCard.entityType(), 10, 1, 4),
                            )
                        ),
                    ),
                    GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                    StructureTerrainAdaptation.BURY,
                ),
                startPool = Registries.TEMPLATE_POOL[mainTemplatePoolKey],
                size = 12,
                startHeight = UniformHeightProvider.of(YOffset.absolute(-40), YOffset.absolute(20)),
                useExpansionHack = false,
            )
        }

        val structureSetKey = registerDynamicGeneration(Registries.STRUCTURE_SET, identifier) {
            StructureSet(
                listOf(
                    StructureSet.StructureSelectionEntry(Registries.STRUCTURE[structureKey], 1),
                ),
                RandomSpreadStructurePlacement(42, 12, SpreadType.LINEAR, 645172983),
            )
        }

        advancement.init()

    }
}
