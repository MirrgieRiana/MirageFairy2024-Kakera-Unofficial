package miragefairy2024.mod.tool.items

import miragefairy2024.mod.tool.ToolConfiguration
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.tags.BlockTags
import net.minecraft.core.BlockPos
import net.minecraft.world.item.Tiers
import net.minecraft.world.level.Level

interface FairyToolItem {
    val configuration: ToolConfiguration
}


fun <I> I.getMiningSpeedMultiplierImpl(@Suppress("UNUSED_PARAMETER") stack: ItemStack, state: BlockState): Float where I : Item, I : FairyToolItem {
    val miningSpeedMultiplier = configuration.miningSpeedMultiplierOverride ?: configuration.toolMaterialCard.toolMaterial.speed
    return when {
        configuration.superEffectiveBlocks.any { state.`is`(it) } -> miningSpeedMultiplier * 10F
        configuration.effectiveBlocks.any { state.`is`(it) } -> miningSpeedMultiplier
        configuration.effectiveBlockTags.any { state.`is`(it) } -> miningSpeedMultiplier
        else -> 1.0F
    }
}

fun <I> I.isSuitableForImpl(state: BlockState): Boolean where I : Item, I : FairyToolItem {
    val itemMiningLevel = configuration.toolMaterialCard.toolMaterial.level
    return when {
        itemMiningLevel < Tiers.DIAMOND.level && state.`is`(BlockTags.NEEDS_DIAMOND_TOOL) -> false
        itemMiningLevel < Tiers.IRON.level && state.`is`(BlockTags.NEEDS_IRON_TOOL) -> false
        itemMiningLevel < Tiers.STONE.level && state.`is`(BlockTags.NEEDS_STONE_TOOL) -> false
        else -> when {
            configuration.superEffectiveBlocks.any { state.`is`(it) } -> true
            configuration.effectiveBlocks.any { state.`is`(it) } -> true
            configuration.effectiveBlockTags.any { state.`is`(it) } -> true
            else -> false
        }
    }
}

fun <I> I.postMineImpl(stack: ItemStack, world: Level, state: BlockState, pos: BlockPos, miner: LivingEntity) where I : Item, I : FairyToolItem {
    configuration.onPostMineListeners.forEach {
        it(this, stack, world, state, pos, miner)
    }
}

fun <I> I.onAfterBreakBlock(world: Level, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) where I : Item, I : FairyToolItem {
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

fun <I> I.inventoryTickImpl(stack: ItemStack, world: Level, entity: Entity, slot: Int, selected: Boolean) where I : Item, I : FairyToolItem {
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
