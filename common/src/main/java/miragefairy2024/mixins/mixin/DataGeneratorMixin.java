package miragefairy2024.mixins.mixin;

import net.minecraft.data.DataGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataGenerator.class)
public class DataGeneratorMixin {
    @Inject(method = "run", at = @At("TAIL"))
    private void run(CallbackInfo ci) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        System.exit(0); // なぜかArchitecturyに移行したらrunDatagenが終わった後プロセスが地縛霊になるようになったので強制終了させる
    }
}
