package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.mod.haimeviska.registerBlockFamily
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.material.PushReaction as PistonBehavior

class HaimeviskaPlanksButtonBlockCard(configuration: HaimeviskaBlockConfiguration, private val blockSetType: () -> BlockSetType, private val parent: () -> Block) : HaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = ButtonBlock(
        blockSetType(),
        30,
        AbstractBlock.Properties.of()
            .noCollission()
            .strength(0.5F)
            .pushReaction(PistonBehavior.DESTROY),
    )

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(parent) { it.button(block()) }
        block.registerDefaultLootTableGeneration()

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_BUTTONS }
        item.registerItemTagGeneration { ItemTags.WOODEN_BUTTONS }

    }
}
