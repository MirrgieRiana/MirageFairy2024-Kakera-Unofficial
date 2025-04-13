package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockCallback {
    public final static Event<AfterBreak> AFTER_BREAK = EventFactory.createArrayBacked(AfterBreak.class, callbacks -> (world, player, pos, state, blockEntity, tool) -> {
        for (AfterBreak callback : callbacks) {
            callback.afterBreak(world, player, pos, state, blockEntity, tool);
        }
    });

    public interface AfterBreak {
        void afterBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool);
    }
}
