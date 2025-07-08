package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SlabBlock

class HaimeviskaPlanksSlabBlockCard(configuration: HaimeviskaBlockConfiguration, private val baseBlock: () -> Registration<*, out Block>) : HaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = SlabBlock(createPlankSettings())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(baseBlock()) { it.slab(block()) }
        block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_SLABS }
        item.registerItemTagGeneration { ItemTags.WOODEN_SLABS }

    }
}
