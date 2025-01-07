package miragefairy2024.lib

import com.mojang.serialization.Codec
import miragefairy2024.mod.placeditem.PlacedItemBlockEntity
import miragefairy2024.mod.placeditem.PlacedItemCard
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.FeatureConfig
import net.minecraft.world.gen.feature.util.FeatureContext

abstract class PlacedItemFeature<C : FeatureConfig>(codec: Codec<C>) : Feature<C>(codec) {
    abstract fun getCount(context: FeatureContext<C>): Int
    abstract fun createItemStack(context: FeatureContext<C>): ItemStack?
    override fun generate(context: FeatureContext<C>): Boolean {
        val random = context.random
        val world = context.world

        var count = 0
        val currentBlockPos = BlockPos.Mutable()
        repeat(getCount(context)) {
            currentBlockPos.set(
                context.origin,
                random.nextInt(3) - random.nextInt(3),
                random.nextInt(3) - random.nextInt(3),
                random.nextInt(3) - random.nextInt(3),
            )

            // 座標決定
            val actualBlockPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, currentBlockPos)

            // 生成環境判定
            if (!world.getBlockState(actualBlockPos).isReplaceable) return@repeat // 配置先が埋まっている
            if (!world.getBlockState(actualBlockPos.down()).isOpaqueFullCube(world, actualBlockPos.down())) return@repeat // 配置先が地面でない

            // アイテム判定
            val itemStack = createItemStack(context) ?: return@repeat // アイテムを決定できなかった

            // 成功

            world.setBlockState(actualBlockPos, PlacedItemCard.block.defaultState, Block.NOTIFY_LISTENERS)
            val blockEntity = world.getBlockEntity(actualBlockPos) as? PlacedItemBlockEntity ?: return@repeat // ブロックの配置に失敗した
            blockEntity.itemStack = itemStack
            blockEntity.itemX = 0.25 + 0.5 * random.nextDouble()
            blockEntity.itemZ = 0.25 + 0.5 * random.nextDouble()
            blockEntity.itemRotateY = MathHelper.TAU * random.nextDouble()
            blockEntity.markDirty()

            count++
        }
        return count > 0
    }
}
