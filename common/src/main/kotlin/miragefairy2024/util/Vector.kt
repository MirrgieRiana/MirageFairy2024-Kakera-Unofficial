package miragefairy2024.util

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.VoxelShape

fun Vec3.toBlockPos(): BlockPos = BlockPos.containing(this)

fun BlockPos.toBox() = AABB(this)

fun createCuboidShape(radius: Double, height: Double): VoxelShape = Block.box(8 - radius, 0.0, 8 - radius, 8 + radius, height, 8 + radius)
