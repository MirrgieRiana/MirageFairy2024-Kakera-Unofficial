package miragefairy2024.mod.magicplant

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.level.Level

// api

interface TraitCondition {
    val emoji: Component
    val name: Component
    fun getFactor(world: Level, blockPos: BlockPos, blockEntity: MagicPlantBlockEntity?): Double
}
