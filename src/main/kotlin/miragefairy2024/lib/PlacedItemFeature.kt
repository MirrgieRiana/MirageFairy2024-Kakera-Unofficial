package miragefairy2024.lib

import com.mojang.serialization.Codec
import miragefairy2024.mod.placeditem.PlacedItemBlockEntity
import miragefairy2024.mod.placeditem.PlacedItemCard
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth as MathHelper
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration as FeatureConfig
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext as FeatureContext

abstract class PlacedItemFeature<C : FeatureConfig>(codec: Codec<C>) : Feature<C>(codec) {
    abstract fun getCount(context: FeatureContext<C>): Int
    abstract fun createItemStack(context: FeatureContext<C>): ItemStack?
    override fun generate(context: FeatureContext<C>): Boolean {
        val random = context.random
        val world = context.level()

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
            val actualBlockPos = world.getTopPosition(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, currentBlockPos)

            // 生成環境判定
            if (!world.getBlockState(actualBlockPos).canBeReplaced()) return@repeat // 配置先が埋まっている
            if (!world.getBlockState(actualBlockPos.below()).isOpaqueFullCube(world, actualBlockPos.below())) return@repeat // 配置先が地面でない

            // アイテム判定
            val itemStack = createItemStack(context) ?: return@repeat // アイテムを決定できなかった

            // 成功

            world.setBlock(actualBlockPos, PlacedItemCard.block.defaultBlockState(), Block.UPDATE_CLIENTS)
            val blockEntity = world.getBlockEntity(actualBlockPos) as? PlacedItemBlockEntity ?: return@repeat // ブロックの配置に失敗した
            blockEntity.itemStack = itemStack
            blockEntity.itemX = (4.0 + 8.0 * random.nextDouble()) / 16.0
            blockEntity.itemY = 0.5 / 16.0
            blockEntity.itemZ = (4.0 + 8.0 * random.nextDouble()) / 16.0
            blockEntity.itemRotateY = MathHelper.TWO_PI * random.nextDouble()
            blockEntity.updateShapeCache()
            blockEntity.setChanged()

            count++
        }
        return count > 0
    }
}
