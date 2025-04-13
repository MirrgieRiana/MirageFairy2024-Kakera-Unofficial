package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface EatFoodCallback {
    Event<EatFoodCallback> EVENT = EventFactory.createArrayBacked(EatFoodCallback.class, callbacks -> (entity, world, stack) -> {
        for (EatFoodCallback callback : callbacks) {
            callback.eatFood(entity, world, stack);
        }
    });

    void eatFood(LivingEntity entity, Level world, ItemStack stack);
}
