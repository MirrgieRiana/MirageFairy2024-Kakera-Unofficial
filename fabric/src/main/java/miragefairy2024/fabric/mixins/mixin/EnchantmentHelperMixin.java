package miragefairy2024.fabric.mixins.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import miragefairy2024.fabric.mixins.impl.ItemEnchantmentsConverterHelper;
import miragefairy2024.mixins.api.ItemFilteringEnchantment;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"))
    private static void getPossibleEntries(int power, ItemStack stack, Stream<Holder<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentInstance>> info) {
        info.getReturnValue().removeIf(entry -> entry.enchantment instanceof ItemFilteringEnchantment && !((ItemFilteringEnchantment) entry.enchantment).isAcceptableItemOnEnchanting(stack));
    }

    @Inject(
            method = "getItemEnchantmentLevel",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    private static void getItemEnchantmentLevel(
            Holder<Enchantment> enchantment,
            ItemStack stack,
            CallbackInfoReturnable<Integer> cir,
            @Local(ordinal = 0) LocalRef<ItemEnchantments> itemEnchantments
    ) {
        itemEnchantments.set(ItemEnchantmentsConverterHelper.convertItemEnchantments(stack, itemEnchantments.get()));
    }

    @Inject(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    private static void runIterationOnItem(
            ItemStack stack,
            EnchantmentHelper.EnchantmentVisitor visitor,
            CallbackInfo ci,
            @Local(ordinal = 0) LocalRef<ItemEnchantments> itemEnchantments
    ) {
        itemEnchantments.set(ItemEnchantmentsConverterHelper.convertItemEnchantments(stack, itemEnchantments.get()));
    }

    @Inject(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;",
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    private static void runIterationOnItem(
            ItemStack stack,
            EquipmentSlot slot,
            LivingEntity entity,
            EnchantmentHelper.EnchantmentInSlotVisitor visitor,
            CallbackInfo ci,
            @Local(ordinal = 0) LocalRef<ItemEnchantments> itemEnchantments
    ) {
        itemEnchantments.set(ItemEnchantmentsConverterHelper.convertItemEnchantments(stack, itemEnchantments.get()));
    }

    @Inject(
            method = "hasTag",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0,
                    shift = At.Shift.BY,
                    by = 2
            )
    )
    private static void hasTag(
            ItemStack stack,
            TagKey<Enchantment> tag,
            CallbackInfoReturnable<Boolean> cir,
            @Local(ordinal = 0) LocalRef<ItemEnchantments> itemEnchantments
    ) {
        itemEnchantments.set(ItemEnchantmentsConverterHelper.convertItemEnchantments(stack, itemEnchantments.get()));
    }
}
