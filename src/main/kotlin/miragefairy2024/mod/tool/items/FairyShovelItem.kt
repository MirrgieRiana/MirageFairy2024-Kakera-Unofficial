package miragefairy2024.mod.tool.items

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ShovelItem
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World

class FairyShovelConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairyShovelItem(this, Item.Properties())

    init {
        this.attackDamage = 1.5F
        this.attackSpeed = -3.0F
        this.tags += ItemTags.SHOVELS
        this.effectiveBlockTags += BlockTags.SHOVEL_MINEABLE
    }
}

class FairyShovelItem(override val configuration: FairyMiningToolConfiguration, settings: Properties) :
    ShovelItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, settings),
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
