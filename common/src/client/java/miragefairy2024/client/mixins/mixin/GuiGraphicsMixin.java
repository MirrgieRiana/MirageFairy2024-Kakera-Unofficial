package miragefairy2024.client.mixins.mixin;

import miragefairy2024.client.mixins.api.RenderingEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "TAIL"))
    private void renderItemDecorations(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        RenderingEvent.RENDER_ITEM_DECORATIONS.invoker().renderItemDecorations((GuiGraphics) (Object) this, font, stack, x, y, text);
    }
}
