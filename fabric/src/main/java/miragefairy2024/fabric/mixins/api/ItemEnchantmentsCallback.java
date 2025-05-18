package miragefairy2024.fabric.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public interface ItemEnchantmentsCallback {
    Event<ItemEnchantmentsCallback> EVENT = EventFactory.createArrayBacked(ItemEnchantmentsCallback.class, callbacks -> (itemStack, mutableItemEnchantments) -> {
        for (ItemEnchantmentsCallback callback : callbacks) {
            callback.modifyItemEnchantments(itemStack, mutableItemEnchantments);
        }
    });

    void modifyItemEnchantments(ItemStack itemStack, ItemEnchantments.Mutable mutableItemEnchantments);
}
