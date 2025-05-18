package miragefairy2024.fabric.mixins.impl;

import miragefairy2024.fabric.mixins.api.ItemEnchantmentsCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ItemEnchantmentsConverterHelper {
    public static ItemEnchantments convertItemEnchantments(ItemStack itemStack, ItemEnchantments itemEnchantments) {
        ItemEnchantments.Mutable mutableItemEnchantments = new ItemEnchantments.Mutable(itemEnchantments);
        ItemEnchantmentsCallback.EVENT.invoker().modifyItemEnchantments(itemStack, mutableItemEnchantments);
        return mutableItemEnchantments.toImmutable();
    }

    public static ItemStack convertItemStack(ItemStack itemStack) {
        ItemStack itemStack2 = itemStack.copy();
        ItemEnchantments.Mutable mutableItemEnchantments = new ItemEnchantments.Mutable(itemStack2.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        ItemEnchantmentsCallback.EVENT.invoker().modifyItemEnchantments(itemStack2, mutableItemEnchantments);
        itemStack2.set(DataComponents.ENCHANTMENTS, mutableItemEnchantments.toImmutable());
        return itemStack2;
    }
}
