package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface EatFoodCallback {
    Event<EatFoodCallback> EVENT = EventFactory.createArrayBacked(EatFoodCallback.class, callbacks -> (entity, world, stack) -> {
        for (EatFoodCallback callback : callbacks) {
            callback.eatFood(entity, world, stack);
        }
    });

    void eatFood(LivingEntity entity, World world, ItemStack stack);
}
