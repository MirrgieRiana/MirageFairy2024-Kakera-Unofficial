package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;

public interface ItemPredicateConvertorCallback {
    Event<ItemPredicateConvertorCallback> EVENT = EventFactory.createArrayBacked(ItemPredicateConvertorCallback.class, callbacks -> itemStack -> {
        ItemStack itemStack2 = itemStack;
        for (ItemPredicateConvertorCallback callback : callbacks) {
            itemStack2 = callback.convertItemStack(itemStack2);
        }
        return itemStack2;
    });

    ItemStack convertItemStack(ItemStack itemStack);
}
