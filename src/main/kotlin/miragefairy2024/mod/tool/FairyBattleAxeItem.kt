package miragefairy2024.mod.tool

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.item.Vanishable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FairyBattleAxeItem(override val fairyToolSettings: FairyToolSettings<FairyBattleAxeItem>, settings: Settings) :
    BattleAxeItem(fairyToolSettings.toolMaterialCard.toolMaterial, fairyToolSettings.attackDamage, fairyToolSettings.attackSpeed, settings),
    FairyToolItem<FairyBattleAxeItem>,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {
    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(this, stack, state)
    override fun isSuitableFor(state: BlockState) = isSuitableForImpl(this, state)

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.postMine(stack, world, state, pos, miner)
        postMineImpl(this, stack, world, state, pos, miner)
        return true
    }

    override fun postHit(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.postHit(stack, target, attacker)
        postHitImpl(this, stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(this, stack, world, entity, slot, selected)
    }

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = overrideEnchantmentLevelImpl(this, enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(this, itemStack)
}

open class BattleAxeItem(toolMaterial: ToolMaterial, attackDamage: Float, attackSpeed: Float, settings: Item.Settings) : ToolItem(toolMaterial, settings), Vanishable {
    private val attackDamage = attackDamage + toolMaterial.attackDamage
    private val attributeModifiers = ImmutableMultimap.builder<EntityAttribute, EntityAttributeModifier>().also {
        it.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", attackDamage.toDouble(), EntityAttributeModifier.Operation.ADDITION))
        it.put(EntityAttributes.GENERIC_ATTACK_SPEED, EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", attackSpeed.toDouble(), EntityAttributeModifier.Operation.ADDITION))
    }.build()

    override fun postHit(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        stack.damage(1, attacker) { e ->
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
        }
        return true
    }

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        if (state.getHardness(world, pos) != 0.0F) {
            stack.damage(2, miner) { e ->
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
            }
        }
        return true
    }

    override fun getAttributeModifiers(slot: EquipmentSlot): Multimap<EntityAttribute, EntityAttributeModifier> {
        return if (slot == EquipmentSlot.MAINHAND) attributeModifiers else super.getAttributeModifiers(slot)
    }
}
