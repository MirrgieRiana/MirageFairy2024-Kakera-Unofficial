package miragefairy2024.mod.tool.items

import miragefairy2024.mod.tool.ToolConfiguration
import net.fabricmc.yarn.constants.MiningLevels
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface FairyToolItem {
    val configuration: ToolConfiguration
}


fun <I> I.getMiningSpeedMultiplierImpl(@Suppress("UNUSED_PARAMETER") stack: ItemStack, state: BlockState): Float where I : Item, I : FairyToolItem {
    val miningSpeedMultiplier = configuration.miningSpeedMultiplierOverride ?: configuration.toolMaterialCard.toolMaterial.miningSpeedMultiplier
    return when {
        configuration.superEffectiveBlocks.any { state.isOf(it) } -> miningSpeedMultiplier * 10F
        configuration.effectiveBlocks.any { state.isOf(it) } -> miningSpeedMultiplier
        configuration.effectiveBlockTags.any { state.isIn(it) } -> miningSpeedMultiplier
        else -> 1.0F
    }
}

fun <I> I.isSuitableForImpl(state: BlockState): Boolean where I : Item, I : FairyToolItem {
    val itemMiningLevel = configuration.toolMaterialCard.toolMaterial.miningLevel
    return when {
        itemMiningLevel < MiningLevels.DIAMOND && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) -> false
        itemMiningLevel < MiningLevels.IRON && state.isIn(BlockTags.NEEDS_IRON_TOOL) -> false
        itemMiningLevel < MiningLevels.STONE && state.isIn(BlockTags.NEEDS_STONE_TOOL) -> false
        else -> when {
            configuration.superEffectiveBlocks.any { state.isOf(it) } -> true
            configuration.effectiveBlocks.any { state.isOf(it) } -> true
            configuration.effectiveBlockTags.any { state.isIn(it) } -> true
            else -> false
        }
    }
}

fun <I> I.postMineImpl(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity) where I : Item, I : FairyToolItem {
    configuration.onPostMineListeners.forEach {
        it(this, stack, world, state, pos, miner)
    }
}

fun <I> I.onAfterBreakBlock(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) where I : Item, I : FairyToolItem {
    configuration.onAfterBreakBlockListeners.forEach {
        it(this, world, player, pos, state, blockEntity, tool)
    }
}

fun <I> I.postHitImpl(@Suppress("UNUSED_PARAMETER") stack: ItemStack, target: LivingEntity, attacker: LivingEntity) where I : Item, I : FairyToolItem {

}

fun <I> I.onKilled(entity: LivingEntity, attacker: LivingEntity, damageSource: DamageSource) where I : Item, I : FairyToolItem {
    configuration.onKilledListeners.forEach {
        it(this, entity, attacker, damageSource)
    }
}

fun <I> I.inventoryTickImpl(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) where I : Item, I : FairyToolItem {
    configuration.onInventoryTickListeners.forEach {
        it(this, stack, world, entity, slot, selected)
    }
}

fun <I> I.overrideEnchantmentLevelImpl(enchantment: Enchantment, @Suppress("UNUSED_PARAMETER") itemStack: ItemStack, oldLevel: Int): Int where I : Item, I : FairyToolItem {
    return configuration.onOverrideEnchantmentLevelListeners.fold(oldLevel) { level, listener -> listener(this, enchantment, level) }
}

fun <I> I.convertItemStackImpl(itemStack: ItemStack): ItemStack where I : Item, I : FairyToolItem {
    return configuration.onConvertItemStackListeners.fold(itemStack) { itemStack2, listener -> listener(this, itemStack2) }
}

fun <I> I.hasGlintImpl(stack: ItemStack): Boolean where I : Item, I : FairyToolItem {
    return configuration.hasGlint
}
