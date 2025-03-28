package miragefairy2024.mod.tool.items

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.HoeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.roundToInt

/**
 * @param attackDamage wood: 0, stone: -1, gold: 0, iron: -2, diamond: -3, netherite: -4
 * @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0
 */
class FairyHoeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    attackDamage: Int,
    attackSpeed: Float,
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairyHoeItem(this, Item.Settings())

    init {
        this.attackDamage = attackDamage.toFloat()
        this.attackSpeed = attackSpeed
        this.tags += ItemTags.HOES
        this.effectiveBlockTags += BlockTags.HOE_MINEABLE
    }
}

class FairyHoeItem(override val configuration: FairyMiningToolConfiguration, settings: Settings) :
    HoeItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage.roundToInt(), configuration.attackSpeed, settings),
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

    override fun hasGlint(stack: ItemStack) = super.hasGlint(stack) || hasGlintImpl(stack)

}
