package miragefairy2024.lib

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.item.context.BlockPlaceContext as ItemPlacementContext
import net.minecraft.world.level.block.state.StateDefinition as StateManager
import net.minecraft.core.Direction

open class SimpleHorizontalFacingBlock(settings: Settings) : HorizontalFacingBlock(settings) {
    init {
        defaultBlockState = defaultBlockState.setValue(FACING, Direction.NORTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        return defaultBlockState.setValue(FACING, ctx.horizontalDirection.opposite)
    }
}
