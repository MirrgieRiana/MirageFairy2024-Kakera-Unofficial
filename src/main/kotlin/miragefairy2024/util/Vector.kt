package miragefairy2024.util

import net.minecraft.world.level.block.Block
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB as Box
import net.minecraft.world.phys.Vec3 as Vec3d
import net.minecraft.world.phys.shapes.VoxelShape

fun Vec3d.toBlockPos(): BlockPos = BlockPos.containing(this)

fun BlockPos.toBox() = Box(this)

fun createCuboidShape(radius: Double, height: Double): VoxelShape = Block.box(8 - radius, 0.0, 8 - radius, 8 + radius, height, 8 + radius)
