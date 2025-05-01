package miragefairy2024.mixin.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public interface DamageCallback {
    Event<DamageCallback> EVENT = EventFactory.createArrayBacked(DamageCallback.class, callbacks -> (entity, source, amount) -> {
        for (DamageCallback callback : callbacks) {
            amount = callback.modifyDamageAmount(entity, source, amount);
        }
        return amount;
    });

    float modifyDamageAmount(LivingEntity entity, DamageSource source, float amount);
}
