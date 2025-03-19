package miragefairy2024.mod.structure

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.tool.ToolCard
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
import miragefairy2024.util.registerArchaeologyLootTableGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerStructureTagGeneration
import miragefairy2024.util.text
import miragefairy2024.util.times
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.item.map.MapIcon
import net.minecraft.loot.function.EnchantRandomlyLootFunction
import net.minecraft.loot.function.ExplorationMapLootFunction
import net.minecraft.loot.function.SetNameLootFunction
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

    val translation = Translation({ "filled_map.dripstone_caves_ruin" }, "Dripstone Caves Ruin Map", "鍾乳洞の遺跡の地図")
    translation.enJa()

    val archaeologyLootTable = "archaeology/" * identifier
    registerArchaeologyLootTableGeneration(archaeologyLootTable) {
        LootTable(
            LootPool(
                ItemLootPoolEntry(Items.RAW_IRON).weight(10),
                ItemLootPoolEntry(Items.RAW_COPPER).weight(10),
                ItemLootPoolEntry(Items.GOLD_NUGGET).weight(10),
                ItemLootPoolEntry(Items.GLASS_PANE).weight(10),
                ItemLootPoolEntry(MaterialCard.XARPITE.item).weight(10),
                ItemLootPoolEntry(MaterialCard.CHAOS_STONE.item).weight(10),

                ItemLootPoolEntry(ToolCard.AMETHYST_PICKAXE.item).weight(1).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(ToolCard.AMETHYST_AXE.item).weight(1).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(ToolCard.AMETHYST_SHOVEL.item).weight(1).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(ToolCard.AMETHYST_HOE.item).weight(1).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(ToolCard.AMETHYST_SWORD.item).weight(1).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(Items.AMETHYST_SHARD).weight(3),
                ItemLootPoolEntry(Items.BOOK).weight(10).apply(EnchantRandomlyLootFunction.builder()),
                ItemLootPoolEntry(MaterialCard.JEWEL_100.item).weight(3),
                ItemLootPoolEntry(Items.MAP) {
                    apply(ExplorationMapLootFunction.builder().withDestination(structureTag).withDecoration(MapIcon.Type.BANNER_BROWN).withZoom(3).withSkipExistingChunks(false))
                    apply(SetNameLootFunction.builder(text { translation() }))
                }.weight(2),
            ),
        ).randomSequenceId(archaeologyLootTable)
    }

    val element = identifier

    val processorListKey = registerDynamicGeneration(RegistryKeys.PROCESSOR_LIST, identifier) {
        StructureProcessorList(
            BlockIgnoreStructureProcessor(listOf(Blocks.AIR, Blocks.DIRT, Blocks.GRASS_BLOCK)),
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
