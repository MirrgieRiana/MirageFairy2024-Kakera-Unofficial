package miragefairy2024.mod.tool.items

import miragefairy2024.MirageFairy2024
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.EnchantmentCard
import miragefairy2024.mod.magicplant.MagicPlantBlock
import miragefairy2024.mod.magicplant.PostTryPickHandlerItem
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.mod.tool.effects.areaMining
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.max
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tier as ToolMaterial
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ClipContext as RaycastContext
import net.minecraft.world.level.Level

class FairyScytheConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    private val range: Int = 1
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairyScytheItem(this, range, Item.Properties())

    init {
        this.attackDamage = 4.0F
        this.attackSpeed = -3.2F
        this.miningDamage = 0.2
        this.areaMining(range)
        this.tags += ItemTags.SWORDS
        this.superEffectiveBlocks += Blocks.COBWEB
        this.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
    }
}

class FairyScytheItem(override val configuration: FairyMiningToolConfiguration, range: Int, settings: Properties) :
    ScytheItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, range, settings),
    FairyToolItem,
    ItemPredicateConvertorCallback {

    override fun getDestroySpeed(stack: ItemStack, state: BlockState) = getMiningSpeedMultiplierImpl(stack, state)

    override fun isCorrectToolForDrops(state: BlockState) = isSuitableForImpl(state)

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

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = super.overrideEnchantmentLevel(enchantment, itemStack, oldLevel) max overrideEnchantmentLevelImpl(enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(itemStack)

    override fun isFoil(stack: ItemStack) = super.isFoil(stack) || hasGlintImpl(stack)

}

open class ScytheItem(material: ToolMaterial, attackDamage: Float, attackSpeed: Float, private val range: Int, settings: Properties) : SwordItem(material, attackDamage.toInt(), attackSpeed, settings), PostTryPickHandlerItem, OverrideEnchantmentLevelCallback {
    companion object {
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("scythe").toLanguageKey()}.description" }, "Perform area harvesting when used %s", "使用時、範囲収穫 %s")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { DESCRIPTION_TRANSLATION(range.toRomanText()).yellow }
    }

    override fun use(world: Level, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {

        if (!user.isShiftKeyDown) {
            val itemStack = user.getItemInHand(hand)
            val blockHitResult = getPlayerPOVHitResult(world, user, RaycastContext.Fluid.NONE)
            val blockPos = blockHitResult.blockPos
            var effective = false
            // TODO 貫通判定
            (-range..range).forEach { x ->
                (-range..range).forEach { y ->
                    (-range..range).forEach { z ->
                        val targetBlockPos = blockPos.offset(x, y, z)
                        val targetBlockState = world.getBlockState(targetBlockPos)
                        val targetBlock = targetBlockState.block
                        if (targetBlock is MagicPlantBlock) {
                            if (targetBlock.tryPick(world, targetBlockPos, user, itemStack, true, false)) effective = true
                        }
                    }
                }
            }
            if (effective) return TypedActionResult.success(itemStack)
        }

        return super.use(world, user, hand)
    }

    override fun hurtEnemy(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        stack.hurtAndBreak(2, attacker) { e ->
            e.broadcastBreakEvent(EquipmentSlot.MAINHAND)
        }
        return true
    }

    override fun mineBlock(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        if (state.getDestroySpeed(world, pos) != 0.0F) {
            if (miner.random.nextFloat() < 0.2F) {
                stack.hurtAndBreak(1, miner) { e ->
                    e.broadcastBreakEvent(EquipmentSlot.MAINHAND)
                }
            }
        }
        return true
    }

    override fun postTryPick(world: Level, blockPos: BlockPos, player: PlayerEntity?, itemStack: ItemStack, succeed: Boolean) {
        if (world.isClientSide) return
        if (player?.isShiftKeyDown == true) return
        (-1..1).forEach { x ->
            (-1..1).forEach { y ->
                (-1..1).forEach { z ->
                    if (x != 0 || y != 0 || z != 0) {
                        val targetBlockPos = blockPos.offset(x, y, z)
                        val targetBlockState = world.getBlockState(targetBlockPos)
                        val targetBlock = targetBlockState.block
                        if (targetBlock is MagicPlantBlock) {
                            targetBlock.tryPick(world, targetBlockPos, player, itemStack, true, false)
                        }
                    }
                }
            }
        }
    }

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int): Int {
        return if (enchantment == Enchantments.BLOCK_FORTUNE) {
            val enchantments = EnchantmentHelper.getEnchantments(itemStack)
            oldLevel max (enchantments[EnchantmentCard.FERTILITY.enchantment] ?: 0)
        } else {
            oldLevel
        }
    }
}
