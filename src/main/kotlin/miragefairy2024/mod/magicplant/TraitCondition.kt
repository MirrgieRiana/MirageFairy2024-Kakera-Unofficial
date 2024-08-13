package miragefairy2024.mod.magicplant

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

// api

fun interface TraitCondition {
    fun getFactor(world: World, blockPos: BlockPos): Double
}
