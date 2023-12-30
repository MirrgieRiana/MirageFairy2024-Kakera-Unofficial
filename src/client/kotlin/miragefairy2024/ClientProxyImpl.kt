package miragefairy2024

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.client.color.world.BiomeColors
import net.minecraft.client.color.world.FoliageColors
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.BlockRenderView

class ClientProxyImpl : ClientProxy {
    override fun registerItemTooltipCallback(block: (stack: ItemStack, lines: MutableList<Text>) -> Unit) {
        ItemTooltipCallback.EVENT.register { stack, _, lines ->
            block(stack, lines)
        }
    }

    override fun registerCutoutRenderLayer(block: Block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout())
    }

    override fun getClientPlayer(): PlayerEntity? = MinecraftClient.getInstance().player

    override fun getBlockColorProvider(block: Block): BlockColorProvider? {
        val provider = ColorProviderRegistry.BLOCK.get(block) ?: return null
        return BlockColorProvider { blockState, world, blockPos, tintIndex ->
            provider.getColor(blockState, world as BlockRenderView?, blockPos, tintIndex)
        }
    }

    override fun registerBlockColorProvider(block: Block, provider: BlockColorProvider) {
        ColorProviderRegistry.BLOCK.register({ blockState, world, blockPos, tintIndex ->
            provider(blockState, world, blockPos, tintIndex)
        }, block)
    }

    override fun getFoliageBlockColorProvider() = BlockColorProvider { blockState, world, blockPos, tintIndex ->
        if (world == null || blockPos == null) FoliageColors.getDefaultColor() else BiomeColors.getFoliageColor(world as BlockRenderView?, blockPos)
    }

    override fun getItemColorProvider(item: Item): ItemColorProvider? {
        val provider = ColorProviderRegistry.ITEM.get(item) ?: return null
        return ItemColorProvider { itemStack, tintIndex ->
            provider.getColor(itemStack, tintIndex)
        }
    }

    override fun registerItemColorProvider(item: Item, provider: ItemColorProvider) {
        ColorProviderRegistry.ITEM.register({ itemStack, tintIndex ->
            provider(itemStack, tintIndex)
        }, item)
    }
}
