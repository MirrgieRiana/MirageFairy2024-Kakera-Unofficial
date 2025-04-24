package miragefairy2024.mod.tool.items

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PickaxeItem
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class FairyPickaxeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairyPickaxeItem(this, Item.Properties())

    init {
        this.attackDamage = 1F
        this.attackSpeed = -2.8F
        this.tags += ItemTags.PICKAXES
        this.tags += ItemTags.CLUSTER_MAX_HARVESTABLES
        this.effectiveBlockTags += BlockTags.MINEABLE_WITH_PICKAXE
    }
}

class FairyPickaxeItem(override val configuration: FairyMiningToolConfiguration, settings: Properties) :
    PickaxeItem(configuration.toolMaterialCard.toolMaterial, settings.attributes(createAttributes(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed))),
    FairyToolItem,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {

    override fun getDestroySpeed(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(stack, state)

    override fun isCorrectToolForDrops(stack: ItemStack, state: BlockState) = isSuitableForImpl(state)

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        super.mineBlock(stack, world, state, pos, miner)
        postMineImpl(stack, world, state, pos, miner)
        return true
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        super.hurtEnemy(stack, target, attacker)
        postHitImpl(stack, target, attacker)
        return true
    }

    override fun inventoryTick(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        inventoryTickImpl(stack, world, entity, slot, selected)
    }

    override fun overrideEnchantmentLevel(enchantment: Holder<Enchantment>, itemStack: ItemStack, oldLevel: Int) = overrideEnchantmentLevelImpl(enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(itemStack)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}
