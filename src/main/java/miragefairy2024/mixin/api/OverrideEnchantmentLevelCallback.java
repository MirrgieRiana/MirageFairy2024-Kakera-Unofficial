package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public interface OverrideEnchantmentLevelCallback {
    Event<OverrideEnchantmentLevelCallback> EVENT = EventFactory.createArrayBacked(OverrideEnchantmentLevelCallback.class, callbacks -> (enchantment, itemStack, oldLevel) -> {
        int level = oldLevel;
        for (OverrideEnchantmentLevelCallback callback : callbacks) {
            level = callback.overrideEnchantmentLevel(enchantment, itemStack, level);
        }
        return level;
    });

    int overrideEnchantmentLevel(Enchantment enchantment, ItemStack itemStack, int oldLevel);
}
