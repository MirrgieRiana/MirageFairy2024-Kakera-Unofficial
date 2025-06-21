package miragefairy2024.mod.tool.items

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.tool.FairyMiningToolConfiguration
import miragefairy2024.mod.tool.ToolMaterialCard
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.Tier
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * @param attackDamage wood: 0, stone: -1, gold: 0, iron: -2, diamond: -3, netherite: -4
 * @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0
 */
open class FairyHoeConfiguration(
    override val toolMaterialCard: ToolMaterialCard,
    attackDamage: Int,
    attackSpeed: Float,
) : FairyMiningToolConfiguration() {
    override fun createItem(properties: Item.Properties) = FairyHoeItem(this, properties)

    var roughen: Boolean = false

    init {
        this.attackDamage = attackDamage.toFloat()
        this.attackSpeed = attackSpeed
        this.tags += ItemTags.HOES
        this.effectiveBlockTags += BlockTags.MINEABLE_WITH_HOE
    }
}

class FairyHoeItem(override val configuration: FairyHoeConfiguration, settings: Properties) :
    AdvancedHoeItem(configuration.toolMaterialCard.toolMaterial, configuration.roughen, settings.attributes(createAttributes(configuration.toolMaterialCard.toolMaterial, configuration.attackDamage, configuration.attackSpeed))),
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

open class AdvancedHoeItem(toolMaterial: Tier, private val roughen: Boolean, settings: Properties) : HoeItem(toolMaterial, settings) {
    companion object {
        val ROUGHEN_TILLABLES: Map<Block, com.mojang.datafixers.util.Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> = mapOf(
            Blocks.FARMLAND to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.GRASS_BLOCK to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.MYCELIUM to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.PODZOL to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.DIRT_PATH to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.ROOTED_DIRT to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS)),
            Blocks.DIRT to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.COARSE_DIRT.defaultBlockState())),
            Blocks.CRIMSON_NYLIUM to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.NETHERRACK.defaultBlockState())),
            Blocks.WARPED_NYLIUM to com.mojang.datafixers.util.Pair.of(Predicate { true }, changeIntoState(Blocks.NETHERRACK.defaultBlockState())),
        )
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val blockPos = context.clickedPos
        val map = if (roughen && context.player?.isShiftKeyDown != true) ROUGHEN_TILLABLES else TILLABLES
        val pair = map[level.getBlockState(blockPos).block] ?: return InteractionResult.PASS
        val predicate = pair.first
        val consumer = pair.second
        if (!predicate.test(context)) return InteractionResult.PASS
        val player = context.player
        level.playSound(player, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F)
        if (level.isClientSide) return InteractionResult.sidedSuccess(level.isClientSide)
        consumer.accept(context)
        if (player != null) context.itemInHand.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.hand))
        return InteractionResult.sidedSuccess(level.isClientSide)
    }
}
