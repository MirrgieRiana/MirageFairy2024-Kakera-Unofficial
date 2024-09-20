package miragefairy2024.mixin.api;

import net.minecraft.item.ItemStack;

public interface ItemFilteringEnchantment {
    boolean isAcceptableItemOnEnchanting(ItemStack itemStack);
}
