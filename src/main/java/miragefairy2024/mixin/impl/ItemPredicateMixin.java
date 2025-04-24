package miragefairy2024.mixin.impl;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin {
    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "HEAD"))
    public void test(ItemStack stack, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0, argsOnly = true) LocalRef<ItemStack> stack2) {
        ItemStack itemStack = stack2.get();
        if (itemStack.getItem() instanceof ItemPredicateConvertorCallback) itemStack = ((ItemPredicateConvertorCallback) itemStack.getItem()).convertItemStack(itemStack);
        itemStack = ItemPredicateConvertorCallback.EVENT.invoker().convertItemStack(itemStack);
        stack2.set(itemStack);
    }
}
