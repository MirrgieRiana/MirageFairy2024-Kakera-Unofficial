package miragefairy2024.mod.tool.items

import miragefairy2024.MirageFairy2024
import miragefairy2024.mixin.api.ItemPredicateConvertorCallback
import miragefairy2024.mixin.api.OverrideEnchantmentLevelCallback
import miragefairy2024.mod.magicplant.MagicPlantBlock
import miragefairy2024.mod.magicplant.PostTryPickHandlerItem
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.FairyToolItem
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.mod.tool.areaMining
import miragefairy2024.mod.tool.convertItemStackImpl
import miragefairy2024.mod.tool.enchantment
import miragefairy2024.mod.tool.getMiningSpeedMultiplierImpl
import miragefairy2024.mod.tool.inventoryTickImpl
import miragefairy2024.mod.tool.isSuitableForImpl
import miragefairy2024.mod.tool.overrideEnchantmentLevelImpl
import miragefairy2024.mod.tool.postHitImpl
import miragefairy2024.mod.tool.postMineImpl
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.item.ToolMaterial
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.World

class FairyScytheConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    fortune: Int,
) : FairyMiningToolConfiguration() {
    override fun createItem() = FairyScytheItem(this, Item.Settings())

    init {
        this.attackDamage = 4.0F
        this.attackSpeed = -3.2F
        this.miningDamage = 0.2
        this.areaMining()
        this.enchantment(Enchantments.FORTUNE, fortune)
        this.tags += ItemTags.SWORDS
        this.superEffectiveBlocks += Blocks.COBWEB
        this.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
    }
}

class FairyScytheItem(override val configuration: FairyMiningToolConfiguration, settings: Settings) :
    ScytheItem(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed, settings),
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

}

open class ScytheItem(material: ToolMaterial, attackDamage: Float, attackSpeed: Float, settings: Settings) : SwordItem(material, attackDamage.toInt(), attackSpeed, settings), PostTryPickHandlerItem {
    companion object {
        val DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("scythe").toTranslationKey()}.description" }, "Perform area harvesting when used", "使用時、範囲収穫")
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip += text { DESCRIPTION_TRANSLATION().yellow }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {

        if (!user.isSneaking) {
            val itemStack = user.getStackInHand(hand)
            val blockHitResult = raycast(world, user, FluidHandling.NONE)
            val blockPos = blockHitResult.blockPos
            var effective = false
            (-1..1).forEach { x ->
                (-1..1).forEach { y ->
                    (-1..1).forEach { z ->
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
        if (world.isClient) return
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
}
