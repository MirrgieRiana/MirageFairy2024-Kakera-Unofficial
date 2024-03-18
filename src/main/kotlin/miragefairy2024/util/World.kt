package miragefairy2024.util

import net.minecraft.block.Blocks
import net.minecraft.block.FarmlandBlock
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

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
    maxDistance: Int,
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
