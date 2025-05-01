package miragefairy2024.mixin.client;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import miragefairy2024.api.client.InputEventsHandlerKt;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "handleKeybinds", at = @At(value = "HEAD"))
    private void handleInputEvents(CallbackInfo ci) {
        for (Function0<Unit> handler : InputEventsHandlerKt.getInputEventsHandlers()) {
            handler.invoke();
        }
    }
}
