package miragefairy2024.util

import miragefairy2024.BlockColorProvider
import miragefairy2024.ItemColorProvider
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import mirrg.kotlin.hydrogen.castOrThrow
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

context(ModContext)
fun Block.registerCutoutRenderLayer() = ModEvents.onClientInit {
    MirageFairy2024.clientProxy!!.registerCutoutRenderLayer(this)
}

context(ModContext)
fun Block.registerColorProvider(provider: BlockColorProvider) = ModEvents.onClientInit {
    MirageFairy2024.clientProxy!!.registerBlockColorProvider(this, provider)
}

context(ModContext)
fun Block.registerFoliageColorProvider() = this.registerColorProvider { blockState, world, blockPos, tintIndex ->
    MirageFairy2024.clientProxy!!.getFoliageBlockColorProvider().invoke(blockState, world, blockPos, tintIndex)
}

context(ModContext)
fun Item.registerColorProvider(provider: ItemColorProvider) = ModEvents.onClientInit {
    MirageFairy2024.clientProxy!!.registerItemColorProvider(this, provider)
}

context(ModContext)
fun BlockItem.registerRedirectColorProvider() = this.registerColorProvider { itemStack, tintIndex ->
    val block = itemStack.item.castOrThrow<BlockItem>().block
    MirageFairy2024.clientProxy!!.getBlockColorProvider(block)!!.invoke(block.defaultState, null, null, tintIndex)
}


fun RenderingProxy.renderItemStack(itemStack: ItemStack, dotX: Double, dotY: Double, dotZ: Double, scale: Float = 1.0F, rotate: Float = 0.0F) {
    this.stack {
        this.translate(dotX / 16.0, dotY / 16.0, dotZ / 16.0)
        this.scale(scale, scale, scale)
        this.rotateY(rotate)
        this.renderItemStack(itemStack)
    }
}

context(ModContext)
fun <T> BlockEntityType<T>.registerRenderingProxyBlockEntityRendererFactory() where T : BlockEntity, T : RenderingProxyBlockEntity = ModEvents.onClientInit {
    MirageFairy2024.clientProxy!!.registerRenderingProxyBlockEntityRendererFactory(this)
}
