package miragefairy2024.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockCallback {
    public final static Event<AfterBreak> AFTER_BREAK = EventFactory.createArrayBacked(AfterBreak.class, callbacks -> (world, player, pos, state, blockEntity, tool) -> {
        for (AfterBreak callback : callbacks) {
            callback.afterBreak(world, player, pos, state, blockEntity, tool);
        }
    });

    public final static Event<GetDropsByEntity> GET_DROPS_BY_ENTITY = EventFactory.createArrayBacked(GetDropsByEntity.class, callbacks -> (state, level, pos, blockEntity, entity, tool, drops) -> {
        List<ItemStack> newDrops = drops;
        for (GetDropsByEntity callback : callbacks) {
            newDrops = callback.getDrops(state, level, pos, blockEntity, entity, tool, newDrops);
        }
        return newDrops;
    });

    public interface AfterBreak {
        void afterBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool);
    }

    public interface GetDropsByEntity {
        List<ItemStack> getDrops(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack tool, List<ItemStack> drops);
    }
}
