package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface BlockBreakingCallback {
    Event<BlockBreakingCallback> EVENT = EventFactory.createArrayBacked(BlockBreakingCallback.class, callbacks -> (state, player, world, pos, blockBreakingDelta) -> {
        for (BlockBreakingCallback callback : callbacks) {
            blockBreakingDelta = callback.calcBlockBreakingDelta(state, player, world, pos, blockBreakingDelta);
        }
        return blockBreakingDelta;
    });

    float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, float blockBreakingDelta);
}
