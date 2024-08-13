package miragefairy2024.mod.magicplant

import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

// api

interface TraitCondition {
    val emoji: Text
    fun getFactor(world: World, blockPos: BlockPos): Double
}
