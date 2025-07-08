package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FenceBlock

class HaimeviskaPlanksFenceBlockCard(configuration: HaimeviskaBlockConfiguration, private val parent: () -> Block) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = FenceBlock(createPlankSettings())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(parent) { it.fence(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_FENCES }
        item.registerItemTagGeneration { ItemTags.WOODEN_FENCES }

    }
}
