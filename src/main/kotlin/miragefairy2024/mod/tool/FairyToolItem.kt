package miragefairy2024.mod.tool

import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.randomInt
import miragefairy2024.util.repair
import mirrg.kotlin.hydrogen.atLeast
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.fabricmc.yarn.constants.MiningLevels
import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun FairyPickaxeItem.getMiningSpeedMultiplierImpl(stack: ItemStack, state: BlockState) = if (toolSettings.effectiveBlockTags.any { state.isIn(it) }) toolSettings.toolMaterialCard.toolMaterial.miningSpeedMultiplier else 1.0F

fun FairyPickaxeItem.isSuitableForImpl(state: BlockState): Boolean {
    val itemMiningLevel = material.miningLevel
    return when {
        itemMiningLevel < MiningLevels.DIAMOND && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) -> false
        itemMiningLevel < MiningLevels.IRON && state.isIn(BlockTags.NEEDS_IRON_TOOL) -> false
        itemMiningLevel < MiningLevels.STONE && state.isIn(BlockTags.NEEDS_STONE_TOOL) -> false
        else -> toolSettings.effectiveBlockTags.any { state.isIn(it) }
    }
}

fun FairyPickaxeItem.postMineImpl(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: LivingEntity) {
    if (toolSettings.areaMining) run fail@{
        if (world.isClient) return@fail

        if (miner.isSneaking) return@fail // 使用者がスニーク中
        if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
        if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない

        // 発動

        val baseHardness = state.getHardness(world, pos)

        (-1..1).forEach { x ->
            (-1..1).forEach { y ->
                (-1..1).forEach { z ->
                    if (x != 0 || y != 0 || z != 0) {
                        val targetBlockPos = pos.add(x, y, z)
                        if (isSuitableFor(world.getBlockState(targetBlockPos))) run skip@{
                            if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                            if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

                            // 採掘を続行

                            val targetBlockState = world.getBlockState(targetBlockPos)
                            val targetHardness = targetBlockState.getHardness(world, targetBlockPos)
                            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                            if (breakBlockByMagic(stack, world, targetBlockPos, miner)) {
                                if (targetHardness > 0) {
                                    stack.damage(1, miner) {
                                        it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (toolSettings.mineAll) run fail@{
        if (world.isClient) return@fail

        if (miner.isSneaking) return@fail // 使用者がスニーク中
        if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
        if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
        if (!state.isIn(ConventionalBlockTags.ORES)) return@fail // 掘ったブロックが鉱石ではない

        // 発動

        val baseHardness = state.getHardness(world, pos)

        blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 31) { _, _, toBlockPos ->
            world.getBlockState(toBlockPos).block === state.block
        }.forEach skip@{ (_, blockPos) ->
            if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
            if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

            // 採掘を続行

            val targetBlockState = world.getBlockState(blockPos)
            val targetHardness = targetBlockState.getHardness(world, blockPos)
            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
            if (breakBlockByMagic(stack, world, blockPos, miner)) {
                if (targetHardness > 0) {
                    stack.damage(1, miner) {
                        it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                    }
                }
            }
        }
    }
    if (toolSettings.cutAll) run fail@{
        if (world.isClient) return@fail

        if (miner.isSneaking) return@fail // 使用者がスニーク中
        if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
        if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
        if (!state.isIn(BlockTags.LOGS)) return@fail // 掘ったブロックが原木ではない

        // 発動

        val baseHardness = state.getHardness(world, pos)

        val logBlockPosList = mutableListOf<BlockPos>()
        blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 19, neighborType = NeighborType.VERTICES) { _, _, toBlockPos ->
            world.getBlockState(toBlockPos).isIn(BlockTags.LOGS)
        }.forEach skip@{ (_, blockPos) ->
            if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
            if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

            // 採掘を続行

            val targetBlockState = world.getBlockState(blockPos)
            val targetHardness = targetBlockState.getHardness(world, blockPos)
            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
            if (breakBlockByMagic(stack, world, blockPos, miner)) {
                if (targetHardness > 0) {
                    stack.damage(1, miner) {
                        it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                    }
                }
                logBlockPosList += blockPos
            }
        }
        blockVisitor(logBlockPosList, visitOrigins = false, maxDistance = 8) { _, _, toBlockPos ->
            world.getBlockState(toBlockPos).isIn(BlockTags.LEAVES)
        }.forEach skip@{ (_, blockPos) ->
            if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
            if (stack.maxDamage - stack.damage <= 1) return@fail // ツールの耐久値が残り1

            // 採掘を続行

            val targetBlockState = world.getBlockState(blockPos)
            val targetHardness = targetBlockState.getHardness(world, blockPos)
            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
            if (breakBlockByMagic(stack, world, blockPos, miner)) {
                if (targetHardness > 0) {
                    if (miner.random.nextFloat() < 0.1F) {
                        stack.damage(1, miner) {
                            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                        }
                    }
                }
            }
        }
    }
}

fun FairyPickaxeItem.inventoryTickImpl(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
    val selfMending = toolSettings.selfMending
    if (selfMending != null) run {
        if (world.isClient) return@run
        if (entity !is PlayerEntity) return@run // プレイヤーじゃない
        if (stack !== entity.mainHandStack) return@run // メインハンドに持っていない
        stack.repair(world.random.randomInt(1.0 / 60.0 / 20.0) * selfMending)
    }
}

fun FairyPickaxeItem.overrideEnchantmentLevelImpl(enchantment: Enchantment, itemStack: ItemStack, oldLevel: Int): Int {
    if (toolSettings.silkTouch) {
        if (enchantment == Enchantments.SILK_TOUCH) return oldLevel atLeast 1
    }
    return oldLevel
}

fun FairyPickaxeItem.convertItemStackImpl(itemStack: ItemStack): ItemStack {
    var itemStack2 = itemStack
    if (toolSettings.silkTouch) {
        itemStack2 = itemStack2.copy()
        val enchantments = EnchantmentHelper.get(itemStack2)
        enchantments[Enchantments.SILK_TOUCH] = enchantments.getOrElse(Enchantments.SILK_TOUCH) { 0 } atLeast 1
        EnchantmentHelper.set(enchantments, itemStack2)
    }
    return itemStack2
}
