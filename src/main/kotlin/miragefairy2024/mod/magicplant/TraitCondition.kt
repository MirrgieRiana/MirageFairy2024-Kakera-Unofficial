package miragefairy2024.mod.magicplant

import net.minecraft.network.chat.Component as Text
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World

// api

interface TraitCondition {
    val emoji: Text
    val name: Text
    fun getFactor(world: World, blockPos: BlockPos): Double
}
