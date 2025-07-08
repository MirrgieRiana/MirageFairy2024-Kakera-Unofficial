package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.state.properties.WoodType

class HaimeviskaPlanksFenceGateBlockCard(configuration: HaimeviskaBlockConfiguration, private val woodType: () -> WoodType, private val parent: () -> Block) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = FenceGateBlock(woodType(), createPlankSettings(sound = false).forceSolidOn())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(parent) { it.fenceGate(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.FENCE_GATES }
        item.registerItemTagGeneration { ItemTags.FENCE_GATES }
        block.registerBlockTagGeneration { TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "fence_gates/wooden")) }
        item.registerItemTagGeneration { TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "fence_gates/wooden")) }

    }
}
