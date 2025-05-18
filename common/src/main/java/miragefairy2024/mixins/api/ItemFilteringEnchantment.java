package miragefairy2024.mixins.api;

import net.minecraft.world.item.ItemStack;

public interface ItemFilteringEnchantment {
    boolean isAcceptableItemOnEnchanting(ItemStack itemStack);
}
