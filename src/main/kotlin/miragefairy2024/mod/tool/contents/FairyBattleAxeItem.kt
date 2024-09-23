package miragefairy2024.mod.tool.contents

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyToolItem
import miragefairy2024.mod.tool.FairyToolSettings
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.mod.tool.convertItemStackImpl
import miragefairy2024.mod.tool.getMiningSpeedMultiplierImpl
import miragefairy2024.mod.tool.inventoryTickImpl
import miragefairy2024.mod.tool.isSuitableForImpl
import miragefairy2024.mod.tool.overrideEnchantmentLevelImpl
import miragefairy2024.mod.tool.postHitImpl
import miragefairy2024.mod.tool.postMineImpl
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.AxeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun createBattleAxe(toolMaterialCard: ToolMaterialCard, attackDamage: Float, attackSpeed: Float) = object : FairyToolSettings() {
    override val toolMaterialCard = toolMaterialCard
    override fun createItem() = FairyBattleAxeItem(this, Item.Settings())

    init {
        this.attackDamage = attackDamage
        this.attackSpeed = attackSpeed
        this.tags += ItemTags.AXES
        this.effectiveBlockTags += BlockTags.AXE_MINEABLE
    }
}

class FairyBattleAxeItem(override val toolSettings: FairyToolSettings, settings: Settings) :
    BattleAxeItem(toolSettings.toolMaterialCard.toolMaterial, toolSettings.attackDamage, toolSettings.attackSpeed, settings),
    FairyToolItem,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {

    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(stack, state)

    override fun isSuitableFor(state: BlockState) = isSuitableForImpl(state)

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.postMine(stack, world, state, pos, miner)
        postMineImpl(stack, world, state, pos, miner)
        return true
    }

    override fun postHit(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.postHit(stack, target, attacker)
        postHitImpl(stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(stack, world, entity, slot, selected)
    }

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = overrideEnchantmentLevelImpl(enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(itemStack)

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
