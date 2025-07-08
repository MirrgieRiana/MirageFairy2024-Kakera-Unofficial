package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.material.PushReaction as PistonBehavior

class HaimeviskaPlanksButtonBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = ButtonBlock(
        HAIMEVISKA_BLOCK_SET_TYPE,
        30,
        AbstractBlock.Properties.of()
            .noCollission()
            .strength(0.5F)
            .pushReaction(PistonBehavior.DESTROY),
    )

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(PLANKS.block) { it.button(block()) }
        block.registerDefaultLootTableGeneration()

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_BUTTONS }
        item.registerItemTagGeneration { ItemTags.WOODEN_BUTTONS }

    }
}
