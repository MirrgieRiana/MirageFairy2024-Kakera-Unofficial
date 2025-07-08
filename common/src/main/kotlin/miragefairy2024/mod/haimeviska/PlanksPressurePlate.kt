package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.material.PushReaction as PistonBehavior

class HaimeviskaPressurePlateBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = PressurePlateBlock(
        HAIMEVISKA_BLOCK_SET_TYPE,
        createBaseWoodSetting(sound = false)
            .forceSolidOn()
            .noCollission()
            .strength(0.5F)
            .pushReaction(PistonBehavior.DESTROY),
    )

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(PLANKS.block) { it.pressurePlate(block()) }
        block.registerDefaultLootTableGeneration()

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_PRESSURE_PLATES }
        item.registerItemTagGeneration { ItemTags.WOODEN_PRESSURE_PLATES }

    }
}
