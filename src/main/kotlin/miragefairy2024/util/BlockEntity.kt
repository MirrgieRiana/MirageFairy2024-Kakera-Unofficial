package miragefairy2024.util

import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType

fun <A : BlockEntity, E : BlockEntity> checkType(
    actualType: BlockEntityType<A>,
    expectedType: BlockEntityType<E>,
    ticker: BlockEntityTicker<E>,
): BlockEntityTicker<A>? {
    return if (actualType === expectedType) {
        @Suppress("UNCHECKED_CAST")
        ticker as BlockEntityTicker<A>
    } else {
        null
    }
}
