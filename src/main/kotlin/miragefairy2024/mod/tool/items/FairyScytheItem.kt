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
import net.minecraft.world.item.TooltipFlag as TooltipContext
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
import net.minecraft.network.chat.Component as Text
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ClipContext as RaycastContext
import net.minecraft.world.level.Level as World

class FairyScytheConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    private val range: Int = 1
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairyScytheItem(this, range, Item.Settings())

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

class FairyScytheItem(override val configuration: FairyMiningToolConfiguration, range: Int, settings: Settings) :
    ScytheItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, range, settings),
    FairyToolItem,
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

    override fun overrideEnchantmentLevel(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int) = super.overrideEnchantmentLevel(enchantment, itemStack, oldLevel) max overrideEnchantmentLevelImpl(enchantment, itemStack, oldLevel)

    override fun convertItemStack(itemStack: ItemStack) = convertItemStackImpl(itemStack)

    override fun hasGlint(stack: ItemStack) = super.hasGlint(stack) || hasGlintImpl(stack)

}

open class ScytheItem(material: ToolMaterial, attackDamage: Float, attackSpeed: Float, private val range: Int, settings: Settings) : SwordItem(material, attackDamage.toInt(), attackSpeed, settings), PostTryPickHandlerItem, OverrideEnchantmentLevelCallback {
    companion object {
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("scythe").toTranslationKey()}.description" }, "Perform area harvesting when used %s", "使用時、範囲収穫 %s")
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip += text { DESCRIPTION_TRANSLATION(range.toRomanText()).yellow }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {

        if (!user.isSneaking) {
            val itemStack = user.getStackInHand(hand)
            val blockHitResult = raycast(world, user, RaycastContext.FluidHandling.NONE)
            val blockPos = blockHitResult.blockPos
            var effective = false
            // TODO 貫通判定
            (-range..range).forEach { x ->
                (-range..range).forEach { y ->
                    (-range..range).forEach { z ->
                        val targetBlockPos = blockPos.add(x, y, z)
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

    override fun postHit(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        stack.damage(2, attacker) { e ->
            e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
        }
        return true
    }

    override fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity): Boolean {
        if (state.getHardness(world, pos) != 0.0F) {
            if (miner.random.nextFloat() < 0.2F) {
                stack.damage(1, miner) { e ->
                    e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                }
            }
        }
        return true
    }

    override fun postTryPick(world: World, blockPos: BlockPos, player: PlayerEntity?, itemStack: ItemStack, succeed: Boolean) {
        if (world.isClientSide) return
        if (player?.isSneaking == true) return
        (-1..1).forEach { x ->
            (-1..1).forEach { y ->
                (-1..1).forEach { z ->
                    if (x != 0 || y != 0 || z != 0) {
                        val targetBlockPos = blockPos.add(x, y, z)
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
            val enchantments = EnchantmentHelper.get(itemStack)
            oldLevel max (enchantments[EnchantmentCard.FERTILITY.enchantment] ?: 0)
        } else {
            oldLevel
        }
    }
}
