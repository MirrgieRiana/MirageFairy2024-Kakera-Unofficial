package miragefairy2024.client.mixins.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import miragefairy2024.mixin.client.api.RenderItemHandler;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "render", at = @At(value = "HEAD"))
    private void renderItem(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        RenderItemHandler.Companion.getINSTANCE().renderItem(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
    }
}
