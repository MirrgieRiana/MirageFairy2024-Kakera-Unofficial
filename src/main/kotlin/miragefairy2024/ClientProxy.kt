package miragefairy2024

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

var clientProxy: ClientProxy? = null


interface ClientProxy {
    fun registerItemTooltipCallback(block: (stack: ItemStack, lines: MutableList<Text>) -> Unit)
    fun registerCutoutRenderLayer(block: Block)
    fun getClientPlayer(): PlayerEntity?
    fun getBlockColorProvider(block: Block): BlockColorProvider?
    fun registerBlockColorProvider(block: Block, provider: BlockColorProvider)
    fun getFoliageBlockColorProvider(): BlockColorProvider
    fun getItemColorProvider(item: Item): ItemColorProvider?
    fun registerItemColorProvider(item: Item, provider: ItemColorProvider)
    fun <T> registerRenderingProxyBlockEntityRendererFactory(blockEntityType: BlockEntityType<T>) where T : BlockEntity, T : RenderingProxyBlockEntity
}

fun interface BlockColorProvider {
    operator fun invoke(blockState: BlockState, world: BlockView?, blockPos: BlockPos?, tintIndex: Int): Int
}

fun interface ItemColorProvider {
    operator fun invoke(itemStack: ItemStack, tintIndex: Int): Int
}

interface RenderingProxy {
    fun stack(block: () -> Unit)

    fun translate(x: Double, y: Double, z: Double)
    fun scale(x: Float, y: Float, z: Float)
    fun rotateX(rad: Float)
    fun rotateY(rad: Float)
    fun rotateZ(rad: Float)

    fun renderItemStack(itemStack: ItemStack)
}

interface RenderingProxyBlockEntity {
    fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) = Unit
}
