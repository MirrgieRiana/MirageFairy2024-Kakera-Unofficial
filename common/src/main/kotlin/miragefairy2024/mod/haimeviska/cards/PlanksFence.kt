package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FenceBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class HaimeviskaPlanksFenceBlockCard(configuration: HaimeviskaBlockConfiguration, private val parent: () -> Block) : HaimeviskaBlockCard(configuration) {
    override fun createSettings() = createPlankSettings()
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = FenceBlock(properties)

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
