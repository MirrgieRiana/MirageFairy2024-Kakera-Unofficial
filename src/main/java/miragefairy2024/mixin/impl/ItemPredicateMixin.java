package miragefairy2024.mixin.impl;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ItemPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin {
    @Inject(method = "test", at = @At(value = "HEAD"))
    public void test(ItemStack stack, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0, argsOnly = true) LocalRef<ItemStack> stack2) {
        ItemStack itemStack = stack2.get();
        if (itemStack.getItem() instanceof ItemPredicateConvertorCallback) itemStack = ((ItemPredicateConvertorCallback) itemStack.getItem()).convertItemStack(itemStack);
        itemStack = ItemPredicateConvertorCallback.EVENT.invoker().convertItemStack(itemStack);
        stack2.set(itemStack);
    }
}
