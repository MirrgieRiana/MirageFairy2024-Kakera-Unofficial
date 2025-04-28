package miragefairy2024.mod.tool.items

import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.Tool
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.Tier as ToolMaterial
import net.minecraft.world.item.context.UseOnContext as ItemUsageContext

class FairyKnifeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
) : FairyMiningToolConfiguration() {
    override fun createItem(tool: Tool) = FairyKnifeItem(this, FairyToolProperties(tool))

    init {
        this.attackDamage = 2.0F
        this.attackSpeed = -2.4F
        this.tags += ItemTags.SWORDS
        this.superEffectiveBlocks += Blocks.COBWEB
        this.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
    }
}

class FairyKnifeItem(override val configuration: FairyMiningToolConfiguration, settings: Properties) :
    KnifeItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, settings),
    FairyToolItem,
    OverrideEnchantmentLevelCallback,
    ItemPredicateConvertorCallback {

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

open class KnifeItem(material: ToolMaterial, attackDamage: Float, attackSpeed: Float, settings: Properties) : AxeItem(material, settings.attributes(createAttributes(material, attackDamage, attackSpeed))) {
    override fun useOn(context: ItemUsageContext?) = InteractionResult.PASS
    override fun postHurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND)
    }
}
