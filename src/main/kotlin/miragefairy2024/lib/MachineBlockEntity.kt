package miragefairy2024.lib

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.util.math.BlockPos

abstract class MachineBlockEntity<E : MachineBlockEntity<E>>(blockEntityType: BlockEntityType<*>, pos: BlockPos, state: BlockState) : LockableContainerBlockEntity(blockEntityType, pos, state)
