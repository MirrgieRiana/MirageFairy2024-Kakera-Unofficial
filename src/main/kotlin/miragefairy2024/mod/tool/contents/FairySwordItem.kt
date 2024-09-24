package miragefairy2024.mod.tool.contents

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyMiningToolSettings
import miragefairy2024.mod.tool.FairyToolItem
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.mod.tool.convertItemStackImpl
import miragefairy2024.mod.tool.getMiningSpeedMultiplierImpl
import miragefairy2024.mod.tool.inventoryTickImpl
import miragefairy2024.mod.tool.isSuitableForImpl
import miragefairy2024.mod.tool.overrideEnchantmentLevelImpl
import miragefairy2024.mod.tool.postHitImpl
import miragefairy2024.mod.tool.postMineImpl
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FairySwordSettings(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolSettings() {
    override fun createItem() = FairySwordItem(this, Item.Settings())

    init {
        this.attackDamage = 3.0F
        this.attackSpeed = -2.4F
        this.miningSpeedMultiplierOverride = 1.5F
        this.tags += ItemTags.SWORDS
        this.superEffectiveBlocks += Blocks.COBWEB
        this.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
    }
}

class FairySwordItem(override val toolSettings: FairyMiningToolSettings, settings: Settings) :
    SwordItem(toolSettings.toolMaterialCard.toolMaterial, toolSettings.attackDamage.toInt(), toolSettings.attackSpeed, settings),
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
