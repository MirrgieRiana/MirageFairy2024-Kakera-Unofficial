package miragefairy2024.mixin.impl;

import miragefairy2024.mixin.api.BlockBreakingCallback;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "calcBlockBreakingDelta", at = @At("RETURN"), cancellable = true)
    private void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        float blockBreakingDelta = cir.getReturnValue();
        blockBreakingDelta = BlockBreakingCallback.EVENT.invoker().calcBlockBreakingDelta(state, player, world, pos, blockBreakingDelta);
        cir.setReturnValue(blockBreakingDelta);
    }
}
