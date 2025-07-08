package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.StairBlock

class HaimeviskaPlanksStairsBlockCard(configuration: HaimeviskaBlockConfiguration, private val baseBlock: () -> Registration<*, out Block>) : HaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = StairBlock(baseBlock().await().defaultBlockState(), createPlankSettings())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(baseBlock()) { it.stairs(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_STAIRS }
        item.registerItemTagGeneration { ItemTags.WOODEN_STAIRS }

    }
}
