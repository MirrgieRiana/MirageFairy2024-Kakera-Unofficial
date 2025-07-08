package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.SlabBlock

class HaimeviskaSlabBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = SlabBlock(createPlankSettings())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(PLANKS.block) { it.slab(block()) }
        block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_SLABS }
        item.registerItemTagGeneration { ItemTags.WOODEN_SLABS }

    }
}

class HaimeviskaBricksSlabBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = SlabBlock(createPlankSettings())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(BRICKS.block) { it.slab(block()) }
        block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_SLABS }
        item.registerItemTagGeneration { ItemTags.WOODEN_SLABS }

    }
}
