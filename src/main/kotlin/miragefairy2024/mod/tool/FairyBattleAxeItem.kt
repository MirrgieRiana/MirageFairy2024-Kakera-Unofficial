package miragefairy2024.mod.tool

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial
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

open class BattleAxeItem(toolMaterial: ToolMaterial, attackDamage: Float, attackSpeed: Float, settings: Settings) : AxeItem(toolMaterial, attackDamage, attackSpeed, settings) {
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
}
