package miragefairy2024.mod.magicplant

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface TraitFactor {
    fun getFactor(world: World, blockPos: BlockPos): Double
}

@Suppress("FunctionName")
fun TraitCondition(isValid: (world: World, blockPos: BlockPos) -> Boolean) = TraitFactor { world, blockPos ->
    if (isValid(world, blockPos)) 1.0 else 0.0
}
