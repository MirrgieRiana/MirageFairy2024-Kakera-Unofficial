package miragefairy2024.mixin.impl;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import miragefairy2024.mixin.api.DamageCallback;
import miragefairy2024.mixin.api.EatFoodCallback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "eat", at = @At("HEAD"))
    private void eatFood(Level world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        EatFoodCallback.EVENT.invoker().eatFood((LivingEntity) (Object) this, world, stack);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0, argsOnly = true) LocalFloatRef amount2) {
        float newAmount2 = DamageCallback.EVENT.invoker().modifyDamageAmount((LivingEntity) (Object) this, source, amount2.get());
        if (newAmount2 <= 0F) {
            cir.setReturnValue(false);
        } else {
            amount2.set(newAmount2);
        }
    }
}
