package miragefairy2024.mod.tool.items

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World

/**
 * @param attackDamage wood: 6.0, stone: 7.0, gold: 6.0, iron: 6.0, diamond: 5.0, netherite: 5.0
 * @param attackSpeed wood: -3.2, stone: -3.2, gold: -3.0, iron: -3.1, diamond: -3.0, netherite: -3.0
 */
class FairyAxeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    attackDamage: Float,
    attackSpeed: Float,
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairyAxeItem(this, Item.Properties())

    init {
        this.attackDamage = attackDamage
        this.attackSpeed = attackSpeed
        this.tags += ItemTags.AXES
        this.effectiveBlockTags += BlockTags.MINEABLE_WITH_AXE
    }
}

class FairyAxeItem(override val configuration: FairyMiningToolConfiguration, settings: Properties) :
    AxeItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, settings),
    FairyToolItem,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {

    override fun getDestroySpeed(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(stack, state)

    override fun isCorrectToolForDrops(state: BlockState) = isSuitableForImpl(state)

    override fun mineBlock(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.mineBlock(stack, world, state, pos, miner)
        postMineImpl(stack, world, state, pos, miner)
        return true
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.hurtEnemy(stack, target, attacker)
        postHitImpl(stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(stack, world, entity, slot, selected)
    }

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = overrideEnchantmentLevelImpl(enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(itemStack)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}
