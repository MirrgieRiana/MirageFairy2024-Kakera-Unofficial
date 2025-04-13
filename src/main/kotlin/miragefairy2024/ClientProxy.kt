package miragefairy2024

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component as Text
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter as BlockView

var clientProxy: ClientProxy? = null


interface ClientProxy {
    fun registerItemTooltipCallback(block: (stack: ItemStack, lines: MutableList<Text>) -> Unit)
    fun registerCutoutRenderLayer(block: Block)
    fun registerTranslucentRenderLayer(block: Block)
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
    fun renderFixedItemStack(itemStack: ItemStack)
    fun renderCutoutBlock(identifier: Identifier, variant: String?, red: Float, green: Float, blue: Float, light: Int, overlay: Int)
}

interface RenderingProxyBlockEntity {
    fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) = Unit
}
