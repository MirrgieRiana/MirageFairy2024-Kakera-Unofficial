package miragefairy2024.mod.structure

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.RuleStructureProcessor
import miragefairy2024.util.SinglePoolElement
import miragefairy2024.util.StructurePool
import miragefairy2024.util.StructureProcessorList
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.registerChestLootTableGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerStructureTagGeneration
import miragefairy2024.util.text
import miragefairy2024.util.times
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.item.map.MapIcon
import net.minecraft.loot.function.ExplorationMapLootFunction
import net.minecraft.loot.function.SetNameLootFunction
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.structure.StructureSet
import net.minecraft.structure.pool.StructurePool
import net.minecraft.structure.pool.StructurePools
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor
import net.minecraft.structure.processor.GravityStructureProcessor
import net.minecraft.structure.processor.StructureProcessorRule
import net.minecraft.structure.rule.AlwaysTruePosRuleTest
import net.minecraft.structure.rule.AlwaysTrueRuleTest
import net.minecraft.structure.rule.RandomBlockMatchRuleTest
import net.minecraft.structure.rule.blockentity.AppendLootRuleBlockEntityModifier
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.StructureTerrainAdaptation
import net.minecraft.world.gen.YOffset
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement
import net.minecraft.world.gen.chunk.placement.SpreadType
import net.minecraft.world.gen.heightprovider.ConstantHeightProvider
import net.minecraft.world.gen.structure.Structure
import java.util.Optional

context(ModContext)
fun initWeatheredAncientRemnants() {
    val identifier = MirageFairy2024.identifier("weathered_ancient_remnants")

    val structureTag = TagKey.of(RegistryKeys.STRUCTURE, MirageFairy2024.identifier("map_of_weathered_ancient_remnants"))
    MirageFairy2024.identifier("dripstone_caves_ruin").registerStructureTagGeneration { structureTag }

    val translation = Translation({ "filled_map.weathered_ancient_remnants" }, "Weathered Ancient Remnants Map", "風化した旧世代の遺構の地図")
    translation.enJa()

    val archaeologyLootTable = "archaeology/" * identifier
    registerChestLootTableGeneration(archaeologyLootTable) {
        LootTable(
            LootPool(
                ItemLootPoolEntry(Items.MAP) {
                    apply(ExplorationMapLootFunction.builder().withDestination(structureTag).withDecoration(MapIcon.Type.BANNER_BROWN).withZoom(3).withSkipExistingChunks(false))
                    apply(SetNameLootFunction.builder(text { translation() }))
                }.weight(1),
            ) {
                rolls(UniformLootNumberProvider.create(5.0F, 15.0F))
            },
        )
    }

    val element = identifier

    val processorListKey = registerDynamicGeneration(RegistryKeys.PROCESSOR_LIST, identifier) {
        StructureProcessorList(
            BlockIgnoreStructureProcessor(listOf(Blocks.DIRT, Blocks.GRASS_BLOCK)),
            GravityStructureProcessor(Heightmap.Type.OCEAN_FLOOR_WG, -3),
            RuleStructureProcessor(
                StructureProcessorRule(
                    RandomBlockMatchRuleTest(Blocks.GRAVEL, 0.2F),
                    AlwaysTrueRuleTest.INSTANCE,
                    AlwaysTruePosRuleTest.INSTANCE,
                    Blocks.SUSPICIOUS_GRAVEL.defaultState,
                    AppendLootRuleBlockEntityModifier(archaeologyLootTable),
                ),
            ),
        )
    }

    val templatePoolKey = registerDynamicGeneration(RegistryKeys.TEMPLATE_POOL, identifier) {
        StructurePool(
            StructurePools.EMPTY,
            SinglePoolElement(element, processorListKey, StructurePool.Projection.RIGID) to 1,
        )
    }

    val structureKey = registerDynamicGeneration(RegistryKeys.STRUCTURE, identifier) {
        UnlimitedJigsawStructure(
            config = Structure.Config(
                RegistryKeys.BIOME[ConventionalBiomeTags.IN_OVERWORLD],
                mapOf(),
                GenerationStep.Feature.SURFACE_STRUCTURES,
                StructureTerrainAdaptation.NONE,
            ),
            startPool = RegistryKeys.TEMPLATE_POOL[templatePoolKey],
            size = 1,
            projectStartToHeightmap = Optional.of(Heightmap.Type.WORLD_SURFACE_WG),
            startHeight = ConstantHeightProvider.create(YOffset.fixed(0)),
            useExpansionHack = false,
        )
    }

    val structureSetKey = registerDynamicGeneration(RegistryKeys.STRUCTURE_SET, identifier) {
        StructureSet(
            listOf(
                StructureSet.WeightedEntry(RegistryKeys.STRUCTURE[structureKey], 1),
            ),
            RandomSpreadStructurePlacement(16, 8, SpreadType.LINEAR, 94857624),
        )
    }

}
