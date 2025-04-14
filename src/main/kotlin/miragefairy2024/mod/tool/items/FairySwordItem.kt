package miragefairy2024.mod.tool.items

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World

class FairySwordConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairySwordItem(this, Item.Properties())

    init {
        this.attackDamage = 3.0F
        this.attackSpeed = -2.4F
        this.miningSpeedMultiplierOverride = 1.5F
        this.tags += ItemTags.SWORDS
        this.superEffectiveBlocks += Blocks.COBWEB
        this.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
    }
}

class FairySwordItem(override val configuration: FairyMiningToolConfiguration, settings: Properties) :
    SwordItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage.toInt(), configuration.attackSpeed, settings),
    FairyToolItem,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {

    override fun getDestroySpeed(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(stack, state)

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
