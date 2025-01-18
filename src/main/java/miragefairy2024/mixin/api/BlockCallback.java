package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockCallback {
    public final static Event<AfterBreak> AFTER_BREAK = EventFactory.createArrayBacked(AfterBreak.class, callbacks -> (world, player, pos, state, blockEntity, tool) -> {
        for (AfterBreak callback : callbacks) {
            callback.afterBreak(world, player, pos, state, blockEntity, tool);
        }
    });

    public interface AfterBreak {
        void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool);
    }
}
