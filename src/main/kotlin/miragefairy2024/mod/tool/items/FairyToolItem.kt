package miragefairy2024.mod.tool.items

import miragefairy2024.mod.fairy.FairyDreamRecipes
import miragefairy2024.mod.fairy.FairyHistoryContainerExtraPlayerDataCategory
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.fairyHistoryContainer
import miragefairy2024.mod.fairy.getRandomFairy
import miragefairy2024.mod.sync
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.randomInt
import miragefairy2024.util.repair
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.ceilToInt
import mirrg.kotlin.hydrogen.max
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.fabricmc.yarn.constants.MiningLevels
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
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
    val areaMining = configuration.areaMining
    if (areaMining != null) run fail@{
        if (world.isClient) return@fail

        if (miner.isSneaking) return@fail // 使用者がスニーク中
        if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
        if (!isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない

        // 発動

        val baseHardness = state.getHardness(world, pos)

        // TODO 貫通抑制
        (-areaMining..areaMining).forEach { x ->
            (-areaMining..areaMining).forEach { y ->
                (-areaMining..areaMining).forEach { z ->
                    if (x != 0 || y != 0 || z != 0) {
                        val targetBlockPos = pos.add(x, y, z)
                        if (isSuitableFor(world.getBlockState(targetBlockPos))) run skip@{
                            if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                            if (stack.maxDamage - stack.damage <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                            // 採掘を続行

                            val targetBlockState = world.getBlockState(targetBlockPos)
                            val targetHardness = targetBlockState.getHardness(world, targetBlockPos)
                            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                            if (breakBlockByMagic(stack, world, targetBlockPos, miner)) {
                                if (targetHardness > 0) {
                                    val damage = world.random.randomInt(configuration.miningDamage)
                                    if (damage > 0) {
                                        stack.damage(damage, miner) {
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
    }
    if (configuration.mineAll) run fail@{
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
            if (stack.maxDamage - stack.damage <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

            // 採掘を続行

            val targetBlockState = world.getBlockState(blockPos)
            val targetHardness = targetBlockState.getHardness(world, blockPos)
            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
            if (breakBlockByMagic(stack, world, blockPos, miner)) {
                if (targetHardness > 0) {
                    val damage = world.random.randomInt(configuration.miningDamage)
                    if (damage > 0) {
                        stack.damage(damage, miner) {
                            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                        }
                    }
                }
            }
        }
    }
    if (configuration.cutAll) run fail@{
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
            if (stack.maxDamage - stack.damage <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

            // 採掘を続行

            val targetBlockState = world.getBlockState(blockPos)
            val targetHardness = targetBlockState.getHardness(world, blockPos)
            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
            if (breakBlockByMagic(stack, world, blockPos, miner)) {
                if (targetHardness > 0) {
                    val damage = world.random.randomInt(configuration.miningDamage)
                    if (damage > 0) {
                        stack.damage(damage, miner) {
                            it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                        }
                    }
                }
                logBlockPosList += blockPos
            }
        }
        blockVisitor(logBlockPosList, visitOrigins = false, maxDistance = 8) { _, _, toBlockPos ->
            world.getBlockState(toBlockPos).isIn(BlockTags.LEAVES)
        }.forEach skip@{ (_, blockPos) ->
            if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
            if (stack.maxDamage - stack.damage <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

            // 採掘を続行

            val targetBlockState = world.getBlockState(blockPos)
            val targetHardness = targetBlockState.getHardness(world, blockPos)
            if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
            if (breakBlockByMagic(stack, world, blockPos, miner)) {
                if (targetHardness > 0) {
                    if (miner.random.nextFloat() < 0.1F) {
                        val damage = world.random.randomInt(configuration.miningDamage)
                        if (damage > 0) {
                            stack.damage(damage, miner) {
                                it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun <I> I.onAfterBreakBlock(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, tool: ItemStack) where I : Item, I : FairyToolItem {
    configuration.obtainFairy?.let { obtainFairy ->
        if (player !is ServerPlayerEntity) return@let // 使用者がプレイヤーでない

        // モチーフの判定
        val motifSet = FairyDreamRecipes.BLOCK.test(state.block)

        // 抽選
        val result = getRandomFairy(world.random, motifSet, obtainFairy) ?: return@let

        // 入手
        val fairyItemStack = result.motif.createFairyItemStack(condensation = result.condensation, count = result.count)
        world.spawnEntity(ItemEntity(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, fairyItemStack))

        // 妖精召喚履歴に追加
        player.fairyHistoryContainer[result.motif] += result.condensation * result.count
        FairyHistoryContainerExtraPlayerDataCategory.sync(player)

    }
    if (configuration.collection) run {
        if (player.world != world) return@run
        world.getEntitiesByClass(ItemEntity::class.java, Box(pos)) { !it.isSpectator }.forEach {
            it.teleport(player.x, player.y, player.z)
            it.resetPickupDelay()
        }
        world.getEntitiesByClass(ExperienceOrbEntity::class.java, Box(pos)) { !it.isSpectator }.forEach {
            it.teleport(player.x, player.y, player.z)
        }
    }
}

fun <I> I.postHitImpl(@Suppress("UNUSED_PARAMETER") stack: ItemStack, target: LivingEntity, attacker: LivingEntity) where I : Item, I : FairyToolItem {

}

fun <I> I.onKilled(entity: LivingEntity, attacker: LivingEntity, damageSource: DamageSource) where I : Item, I : FairyToolItem {
    configuration.obtainFairy?.let { obtainFairy ->
        if (attacker !is ServerPlayerEntity) return@let // 使用者がプレイヤーでない

        // モチーフの判定
        val motifSet = FairyDreamRecipes.ENTITY_TYPE.test(entity.type)

        // 抽選
        val result = getRandomFairy(entity.world.random, motifSet, obtainFairy) ?: return@let

        // 入手
        val fairyItemStack = result.motif.createFairyItemStack(condensation = result.condensation, count = result.count)
        entity.world.spawnEntity(ItemEntity(entity.world, entity.x, entity.y, entity.z, fairyItemStack))

        // 妖精召喚履歴に追加
        attacker.fairyHistoryContainer[result.motif] += result.condensation * result.count
        FairyHistoryContainerExtraPlayerDataCategory.sync(attacker)

    }
    if (configuration.collection) run {
        if (attacker.world != entity.world) return@run
        entity.world.getEntitiesByClass(ItemEntity::class.java, entity.boundingBox) { !it.isSpectator }.forEach {
            it.teleport(attacker.x, attacker.y, attacker.z)
            it.resetPickupDelay()
        }
        entity.world.getEntitiesByClass(ExperienceOrbEntity::class.java, entity.boundingBox) { !it.isSpectator }.forEach {
            it.teleport(attacker.x, attacker.y, attacker.z)
        }
    }
}

fun <I> I.inventoryTickImpl(stack: ItemStack, world: World, entity: Entity, @Suppress("UNUSED_PARAMETER") slot: Int, @Suppress("UNUSED_PARAMETER") selected: Boolean) where I : Item, I : FairyToolItem {
    val selfMending = configuration.selfMending
    if (selfMending != null) run {
        if (world.isClient) return@run
        if (entity !is PlayerEntity) return@run // プレイヤーじゃない
        if (stack !== entity.mainHandStack) return@run // メインハンドに持っていない
        stack.repair(world.random.randomInt(1.0 / 60.0 / 20.0) * selfMending)
    }
}

fun <I> I.overrideEnchantmentLevelImpl(enchantment: Enchantment, @Suppress("UNUSED_PARAMETER") itemStack: ItemStack, oldLevel: Int): Int where I : Item, I : FairyToolItem {
    val newLevel = configuration.enchantments[enchantment] ?: return oldLevel
    return oldLevel max newLevel
}

fun <I> I.convertItemStackImpl(itemStack: ItemStack): ItemStack where I : Item, I : FairyToolItem {
    var itemStack2 = itemStack
    if ((configuration.enchantments[Enchantments.SILK_TOUCH] ?: 0) >= 1) {
        itemStack2 = itemStack2.copy()
        val enchantments = EnchantmentHelper.get(itemStack2)
        enchantments[Enchantments.SILK_TOUCH] = enchantments.getOrElse(Enchantments.SILK_TOUCH) { 0 } atLeast 1
        EnchantmentHelper.set(enchantments, itemStack2)
    }
    return itemStack2
}

fun <I> I.hasGlintImpl(stack: ItemStack): Boolean where I : Item, I : FairyToolItem {
    return configuration.hasGlint
}
