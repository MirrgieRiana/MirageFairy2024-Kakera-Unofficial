package miragefairy2024.mod.magicplant

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun interface TraitFactor {
    fun getFactor(world: World, blockPos: BlockPos): Double
}

fun interface TraitCondition : TraitFactor {
    override fun getFactor(world: World, blockPos: BlockPos) = if (isValid(world, blockPos)) 1.0 else 0.0
    fun isValid(world: World, blockPos: BlockPos): Boolean
}
