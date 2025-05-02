package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface OverrideEnchantmentLevelCallback {
    Event<OverrideEnchantmentLevelCallback> EVENT = EventFactory.createArrayBacked(OverrideEnchantmentLevelCallback.class, callbacks -> (enchantment, itemStack, oldLevel) -> {
        int level = oldLevel;
        for (OverrideEnchantmentLevelCallback callback : callbacks) {
            level = callback.overrideEnchantmentLevel(enchantment, itemStack, level);
        }
        return level;
    });

    int overrideEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack itemStack, int oldLevel);
}
