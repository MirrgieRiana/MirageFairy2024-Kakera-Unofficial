package miragefairy2024.util

import miragefairy2024.mod.SoundEventCard
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.FarmlandBlock
import net.minecraft.block.OperatorBlock
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

fun BlockView.getMoisture(blockPos: BlockPos): Double {
    val blockState = this.getBlockState(blockPos)
    if (blockState.isOf(Blocks.FARMLAND)) return 0.5 + 0.5 * (blockState.get(FarmlandBlock.MOISTURE) / 7.0)
    if (blockState.isIn(BlockTags.DIRT)) return 0.5
    if (blockState.isIn(BlockTags.SAND)) return 0.25
    return 0.0
}

fun BlockView.getCrystalErg(blockPos: BlockPos): Double {
    // TODO 妖精の継承を使って判定
    return when (getBlockState(blockPos).block) {

        Blocks.DIAMOND_BLOCK -> 1.0

        Blocks.EMERALD_BLOCK -> 0.8
        Blocks.AMETHYST_BLOCK -> 0.8

        Blocks.GOLD_BLOCK -> 0.6
        Blocks.QUARTZ_BLOCK -> 0.6

        Blocks.LAPIS_BLOCK -> 0.4
        Blocks.REDSTONE_BLOCK -> 0.4
        Blocks.IRON_BLOCK -> 0.4

        Blocks.COAL_BLOCK -> 0.2
        Blocks.COPPER_BLOCK -> 0.2

        else -> 0.0
    }
}

enum class NeighborType {
    FACES,
    EDGES,
    VERTICES,
}

fun blockVisitor(
    originalBlockPosList: Iterable<BlockPos>,
    visitOrigins: Boolean = true,
    maxDistance: Int = Int.MAX_VALUE,
    maxCount: Int? = null,
    neighborType: NeighborType = NeighborType.FACES,
    predicate: (fromBlockPos: BlockPos, toBlockPos: BlockPos) -> Boolean,
) = sequence {
    val checkedBlockPosList = mutableSetOf<BlockPos>()
    var nextBlockPosList = originalBlockPosList.toMutableSet()
    var count = 0

    if (maxCount == 0) return@sequence

    (0..maxDistance).forEach { distance ->
        if (nextBlockPosList.isEmpty()) return@sequence

        val currentBlockPosList: Set<BlockPos> = nextBlockPosList
        nextBlockPosList = mutableSetOf()

        currentBlockPosList.forEach nextCurrentBlockPos@{ fromBlockPos ->

            if (distance > 0 || visitOrigins) {
                yield(Pair(distance, fromBlockPos))
                count++
                if (maxCount != null && count >= maxCount) return@sequence
            }

            fun check(offsetX: Int, offsetY: Int, offsetZ: Int) {
                val toBlockPos = fromBlockPos.add(offsetX, offsetY, offsetZ)
                if (toBlockPos !in checkedBlockPosList && predicate(fromBlockPos, toBlockPos)) {
                    checkedBlockPosList += toBlockPos
                    nextBlockPosList += toBlockPos
                }
            }

            check(1, 0, 0)
            check(-1, 0, 0)
            check(0, 1, 0)
            check(0, -1, 0)
            check(0, 0, 1)
            check(0, 0, -1)
            if (neighborType != NeighborType.FACES) {
                check(0, 1, 1)
                check(0, 1, -1)
                check(0, -1, 1)
                check(0, -1, -1)
                check(1, 0, 1)
                check(1, 0, -1)
                check(-1, 0, 1)
                check(-1, 0, -1)
                check(1, 1, 0)
                check(1, -1, 0)
                check(-1, 1, 0)
                check(-1, -1, 0)
                if (neighborType != NeighborType.EDGES) {
                    check(1, 1, 1)
                    check(1, 1, -1)
                    check(1, -1, 1)
                    check(1, -1, -1)
                    check(-1, 1, 1)
                    check(-1, 1, -1)
                    check(-1, -1, 1)
                    check(-1, -1, -1)
                }
            }
        }

    }

}

/**
 * プレイヤーの動作としてブロックを壊します。
 *
 * [ServerPlayerInteractionManager.tryBreakBlock]とは以下の点で異なります。
 * - ブロックの硬度が無限の場合、無効になる。
 */
fun breakBlock(itemStack: ItemStack, world: World, blockPos: BlockPos, player: ServerPlayerEntity): Boolean {
    val blockState = world.getBlockState(blockPos)
    if (!itemStack.item.canMine(blockState, world, blockPos, player)) return false // このツールは採掘そのものができない
    val blockEntity = world.getBlockEntity(blockPos)
    val block = blockState.block

    if (blockState.getHardness(world, blockPos) < 0F) return false // このブロックは破壊不能
    if (block is OperatorBlock && !player.isCreativeLevelTwoOp) {
        world.updateListeners(blockPos, blockState, blockState, Block.NOTIFY_ALL)
        return false // コマンドブロックを破壊しようとした
    }
    if (player.isBlockBreakingRestricted(world, blockPos, player.interactionManager.gameMode)) return false // 破壊する権限がない

    block.onBreak(world, blockPos, blockState, player)
    val success = world.removeBlock(blockPos, false)
    if (success) block.onBroken(world, blockPos, blockState)
    if (player.isCreative) return true // クリエイティブの場合、ドロップを省略
    val newItemStack = itemStack.copy()
    val canHarvest = player.canHarvest(blockState)
    itemStack.postMine(world, blockState, blockPos, player)
    if (success && canHarvest) block.afterBreak(world, player, blockPos, blockState, blockEntity, newItemStack)
    return true
}

/**
 * 魔法効果としてブロックを壊します。
 *
 * [breakBlock]とは以下の点で異なります。
 * - 近接武器の採掘不能特性を無視します。
 * - 専用のツールが必要なブロックを、ツールの種類にかかわらず回収可能です。
 * - [Item.postMine]を起動せず、アイテムの耐久値の減少などが発生しません。
 */
fun breakBlockByMagic(itemStack: ItemStack, world: World, blockPos: BlockPos, player: ServerPlayerEntity): Boolean {
    val blockState = world.getBlockState(blockPos)
    val blockEntity = world.getBlockEntity(blockPos)
    val block = blockState.block

    if (blockState.getHardness(world, blockPos) < 0F) return false // このブロックは破壊不能
    if (block is OperatorBlock && !player.isCreativeLevelTwoOp) {
        world.updateListeners(blockPos, blockState, blockState, Block.NOTIFY_ALL)
        return false // コマンドブロックを破壊しようとした
    }
    if (player.isBlockBreakingRestricted(world, blockPos, player.interactionManager.gameMode)) return false // 破壊する権限がない

    block.onBreak(world, blockPos, blockState, player)
    val success = world.removeBlock(blockPos, false)
    if (success) block.onBroken(world, blockPos, blockState)
    if (player.isCreative) return true // クリエイティブの場合、ドロップを省略
    val newItemStack = itemStack.copy()
    if (success) block.afterBreak(world, player, blockPos, blockState, blockEntity, newItemStack)
    return true
}

fun collectItem(
    world: World,
    player: PlayerEntity,
    originalBlockPos: BlockPos,
    reach: Int,
    maxCount: Int,
) {
    val targetTable = world.getEntitiesByClass(ItemEntity::class.java, Box(originalBlockPos).expand(reach.toDouble())) {
        when {
            it.isSpectator -> false // スペクテイターモードであるアイテムには無反応
            it.boundingBox.intersects(player.boundingBox) -> false // 既に触れているアイテムには無反応
            else -> true
        }
    }.groupBy { it.blockPos }

    var remainingAmount = maxCount
    var processedCount = 0
    if (targetTable.isNotEmpty()) run finish@{
        blockVisitor(listOf(originalBlockPos), maxDistance = reach) { fromBlockPos, toBlockPos ->
            val offset = toBlockPos.subtract(fromBlockPos)
            val direction = when {
                offset.y == -1 -> Direction.DOWN
                offset.y == 1 -> Direction.UP
                offset.z == -1 -> Direction.NORTH
                offset.z == 1 -> Direction.SOUTH
                offset.x == -1 -> Direction.WEST
                offset.x == 1 -> Direction.EAST
                else -> throw AssertionError()
            }
            !world.getBlockState(fromBlockPos).isSideSolidFullSquare(world, fromBlockPos, direction) && !world.getBlockState(toBlockPos).isSideSolidFullSquare(world, toBlockPos, direction.opposite)
        }.forEach { (_, blockPos) ->
            targetTable[blockPos]?.forEach {

                it.teleport(player.x, player.y, player.z)
                it.resetPickupDelay()

                processedCount++

                remainingAmount--
                if (remainingAmount <= 0) return@finish

            }
        }
    }

    if (processedCount > 0) {

        // Effect
        val pos = originalBlockPos.toCenterPos()
        world.playSound(null, pos.x, pos.y, pos.z, SoundEventCard.COLLECT.soundEvent, SoundCategory.PLAYERS, 0.15F, 0.8F + (world.random.nextFloat() - 0.5F) * 0.5F)

    }

}
