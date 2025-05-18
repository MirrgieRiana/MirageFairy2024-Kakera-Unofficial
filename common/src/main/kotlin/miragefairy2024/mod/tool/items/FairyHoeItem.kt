package miragefairy2024.mod.tool.items

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

/**
 * @param attackDamage wood: 0, stone: -1, gold: 0, iron: -2, diamond: -3, netherite: -4
 * @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0
 */
class FairyHoeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    attackDamage: Int,
    attackSpeed: Float,
) : FairyMiningToolConfiguration() {
    override fun createItem(tool: Tool) = FairyHoeItem(this, FairyToolProperties(tool))

    init {
        this.attackDamage = attackDamage.toFloat()
        this.attackSpeed = attackSpeed
        this.tags += ItemTags.HOES
        this.effectiveBlockTags += BlockTags.MINEABLE_WITH_HOE
    }
}

class FairyHoeItem(override val configuration: FairyMiningToolConfiguration, settings: Properties) :
    HoeItem(configuration.toolMaterialCard.toolMaterial, settings.attributes(createAttributes(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed))),
    FairyToolItem,
    ModifyItemEnchantmentsHandler {

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

    override fun modifyItemEnchantments(itemStack: ItemStack, mutableItemEnchantments: ItemEnchantments.Mutable, enchantmentLookup: HolderLookup.RegistryLookup<Enchantment>) = modifyItemEnchantmentsImpl(itemStack, mutableItemEnchantments, enchantmentLookup)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}
