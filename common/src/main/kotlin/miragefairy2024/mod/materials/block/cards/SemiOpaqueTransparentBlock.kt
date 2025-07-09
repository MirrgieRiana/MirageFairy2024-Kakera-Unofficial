package miragefairy2024.mod.materials.block.cards

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.TransparentBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.BlockGetter as BlockView

class SemiOpaqueTransparentBlock(settings: Properties) : TransparentBlock(settings) {
    companion object {
        val CODEC: MapCodec<SemiOpaqueTransparentBlock> = simpleCodec(::SemiOpaqueTransparentBlock)
    }

    override fun codec() = CODEC

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getLightBlock(state: BlockState, world: BlockView, pos: BlockPos) = 1
}
