package miragefairy2024.mixin.impl;

import miragefairy2024.mixin.api.ItemFilteringEnchantment;
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getPossibleEntries", at = @At("RETURN"))
    private static void getPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> info) {
        info.getReturnValue().removeIf(entry -> entry.enchantment instanceof ItemFilteringEnchantment && !((ItemFilteringEnchantment) entry.enchantment).isAcceptableItemOnEnchanting(stack));
    }

    @Inject(method = "getLevel", at = @At("RETURN"), cancellable = true)
    private static void getLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int level = cir.getReturnValue();
        Item item = stack.getItem();
        if (item instanceof OverrideEnchantmentLevelCallback) {
            level = ((OverrideEnchantmentLevelCallback) item).overrideEnchantmentLevel(enchantment, stack, level);
        }
        level = OverrideEnchantmentLevelCallback.EVENT.invoker().overrideEnchantmentLevel(enchantment, stack, level);
        cir.setReturnValue(level);
    }
}
