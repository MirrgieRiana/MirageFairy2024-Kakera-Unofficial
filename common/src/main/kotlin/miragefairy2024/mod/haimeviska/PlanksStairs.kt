package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.StairBlock

class HaimeviskaStairsBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = StairBlock(PLANKS.block.await().defaultBlockState(), createPlankSettings())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(PLANKS.block) { it.stairs(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_STAIRS }
        item.registerItemTagGeneration { ItemTags.WOODEN_STAIRS }

    }
}

class HaimeviskaBricksStairsBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = StairBlock(BRICKS.block.await().defaultBlockState(), createPlankSettings())

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(BRICKS.block) { it.stairs(block()) }
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_STAIRS }
        item.registerItemTagGeneration { ItemTags.WOODEN_STAIRS }

    }
}
