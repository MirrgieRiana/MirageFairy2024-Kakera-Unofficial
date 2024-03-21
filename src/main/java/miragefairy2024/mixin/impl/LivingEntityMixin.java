package miragefairy2024.mixin.impl;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import miragefairy2024.mixin.api.DamageCallback;
import miragefairy2024.mixin.api.EatFoodCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        EatFoodCallback.EVENT.invoker().eatFood((LivingEntity) (Object) this, world, stack);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0, argsOnly = true) LocalFloatRef amount2) {
        float newAmount2 = DamageCallback.EVENT.invoker().modifyDamageAmount((LivingEntity) (Object) this, source, amount2.get());
        if (newAmount2 <= 0F) {
            cir.setReturnValue(false);
        } else {
            amount2.set(newAmount2);
        }
    }
}
