package miragefairy2024.util

import miragefairy2024.BlockColorProvider
import miragefairy2024.ItemColorProvider
import miragefairy2024.MirageFairy2024
import mirrg.kotlin.hydrogen.castOrThrow
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item

fun Block.registerCutoutRenderLayer() {
    MirageFairy2024.onClientInit {
        it.registerCutoutRenderLayer(this)
    }
}

fun Block.registerColorProvider(provider: BlockColorProvider) = MirageFairy2024.onClientInit {
    it.registerBlockColorProvider(this, provider)
}

fun Block.registerFoliageColorProvider() = this.registerColorProvider { blockState, world, blockPos, tintIndex ->
    MirageFairy2024.clientProxy!!.getFoliageBlockColorProvider().invoke(blockState, world, blockPos, tintIndex)
}

fun Item.registerColorProvider(provider: ItemColorProvider) = MirageFairy2024.onClientInit {
    it.registerItemColorProvider(this, provider)
}

fun BlockItem.registerRedirectColorProvider() = this.registerColorProvider { itemStack, tintIndex ->
    val block = itemStack.item.castOrThrow<BlockItem>().block
    MirageFairy2024.clientProxy!!.getBlockColorProvider(block)!!.invoke(block.defaultState, null, null, tintIndex)
}
