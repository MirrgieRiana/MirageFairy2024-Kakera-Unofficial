package miragefairy2024.mod.tool

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FairyMiningToolItem(override val fairyToolSettings: FairyToolSettings<FairyMiningToolItem>, settings: Settings) :
    MiningToolItem(fairyToolSettings.attackDamage, fairyToolSettings.attackSpeed, fairyToolSettings.toolMaterialCard.toolMaterial, BlockTags.PICKAXE_MINEABLE/* dummy */, settings),
    FairyToolItem<FairyMiningToolItem>,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {
    override fun getMiningSpeedMultiplier(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(this, stack, state)
    override fun isSuitableFor(state: BlockState) = isSuitableForImpl(this, state)

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.postMine(stack, world, state, pos, miner)
        postMineImpl(this, stack, world, state, pos, miner)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(this, stack, world, entity, slot, selected)
    }

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = overrideEnchantmentLevelImpl(this, enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(this, itemStack)
}
