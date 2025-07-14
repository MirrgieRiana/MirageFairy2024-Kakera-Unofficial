package miragefairy2024.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public interface EquippedItemBrokenCallback {
    Event<EquippedItemBrokenCallback> EVENT = EventFactory.createArrayBacked(EquippedItemBrokenCallback.class, callbacks -> (entity, item, slot) -> {
        for (EquippedItemBrokenCallback callback : callbacks) {
            callback.onEquippedItemBroken(entity, item, slot);
        }
    });

    void onEquippedItemBroken(LivingEntity entity, Item item, EquipmentSlot slot);
}
