package miragefairy2024.fabric.mixins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import miragefairy2024.fabric.mixins.impl.ItemEnchantmentsConverterHelper;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
public class ItemPredicateMixin {
    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "HEAD"))
    public void test(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0, argsOnly = true) LocalRef<ItemStack> itemStack2) {
        itemStack2.set(ItemEnchantmentsConverterHelper.convertItemStack(itemStack2.get()));
    }
}
