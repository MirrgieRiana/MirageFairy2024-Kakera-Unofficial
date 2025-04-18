package miragefairy2024.mod.magicplant

import net.minecraft.network.chat.Component
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World

// api

interface TraitCondition {
    val emoji: Component
    val name: Component
    fun getFactor(world: World, blockPos: BlockPos): Double
}
