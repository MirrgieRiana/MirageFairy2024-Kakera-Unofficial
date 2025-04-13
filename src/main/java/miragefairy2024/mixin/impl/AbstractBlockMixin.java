package miragefairy2024.mixin.impl;

import miragefairy2024.mixin.api.BlockBreakingCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class AbstractBlockMixin {
    @Inject(method = "calcBlockBreakingDelta", at = @At("RETURN"), cancellable = true)
    private void calcBlockBreakingDelta(BlockState state, Player player, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        float blockBreakingDelta = cir.getReturnValue();
        blockBreakingDelta = BlockBreakingCallback.EVENT.invoker().calcBlockBreakingDelta(state, player, world, pos, blockBreakingDelta);
        cir.setReturnValue(blockBreakingDelta);
    }
}
