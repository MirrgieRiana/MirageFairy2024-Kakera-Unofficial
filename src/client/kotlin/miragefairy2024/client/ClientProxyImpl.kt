package miragefairy2024.client

import miragefairy2024.BlockColorProvider
import miragefairy2024.ClientProxy
import miragefairy2024.ItemColorProvider
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.renderer.BiomeColors
import net.minecraft.world.level.FoliageColor as FoliageColors
import net.minecraft.client.renderer.RenderType as RenderLayer
import net.minecraft.client.renderer.MultiBufferSource as VertexConsumerProvider
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers as BlockEntityRendererFactories
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider as BlockEntityRendererFactory
import net.minecraft.world.item.ItemDisplayContext as ModelTransformationMode
import net.minecraft.client.resources.model.ModelResourceLocation as ModelIdentifier
import com.mojang.blaze3d.vertex.PoseStack as MatrixStack
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component as Text
import net.minecraft.resources.ResourceLocation as Identifier
import com.mojang.math.Axis as RotationAxis
import net.minecraft.world.level.BlockAndTintGetter as BlockRenderView

class ClientProxyImpl : ClientProxy {
    override fun registerItemTooltipCallback(block: (stack: ItemStack, lines: MutableList<Text>) -> Unit) {
        ItemTooltipCallback.EVENT.register { stack, _, lines ->
            block(stack, lines)
        }
    }

    override fun registerCutoutRenderLayer(block: Block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.cutout())
    }

    override fun registerTranslucentRenderLayer(block: Block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.translucent())
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

    override fun getFoliageBlockColorProvider() = BlockColorProvider { _, world, blockPos, _ ->
        if (world == null || blockPos == null) FoliageColors.getDefaultColor() else BiomeColors.getAverageFoliageColor(world as BlockRenderView?, blockPos)
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

    override fun <T> registerRenderingProxyBlockEntityRendererFactory(blockEntityType: BlockEntityType<T>) where T : BlockEntity, T : RenderingProxyBlockEntity {
        BlockEntityRendererFactories.register(blockEntityType, ::RenderingProxyBlockEntityRenderer)
    }
}

class RenderingProxyBlockEntityRenderer<T>(
    @Suppress("UNUSED_PARAMETER") ctx: BlockEntityRendererFactory.Context,
) : BlockEntityRenderer<T> where T : BlockEntity, T : RenderingProxyBlockEntity {
    override fun render(blockEntity: T, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val renderingProxy = object : RenderingProxy {
            override fun stack(block: () -> Unit) {
                matrices.pushPose()
                try {
                    block()
                } finally {
                    matrices.popPose()
                }
            }

            override fun translate(x: Double, y: Double, z: Double) = matrices.translate(x, y, z)
            override fun scale(x: Float, y: Float, z: Float) = matrices.scale(x, y, z)
            override fun rotateX(rad: Float) = matrices.mulPose(RotationAxis.XP.rotation(rad))
            override fun rotateY(rad: Float) = matrices.mulPose(RotationAxis.YP.rotation(rad))
            override fun rotateZ(rad: Float) = matrices.mulPose(RotationAxis.ZP.rotation(rad))

            override fun renderItemStack(itemStack: ItemStack) {
                MinecraftClient.getInstance().itemRenderer.renderStatic(itemStack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, blockEntity.level, 0)
            }

            override fun renderFixedItemStack(itemStack: ItemStack) {
                MinecraftClient.getInstance().itemRenderer.renderStatic(itemStack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, blockEntity.level, 0)
            }

            override fun renderCutoutBlock(identifier: Identifier, variant: String?, red: Float, green: Float, blue: Float, light: Int, overlay: Int) {
                val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.cutout())
                val bakedModel = if (variant != null) {
                    MinecraftClient.getInstance().modelManager.getModel(ModelIdentifier(identifier, variant))
                } else {
                    MinecraftClient.getInstance().modelManager.getModel(identifier)
                }
                MinecraftClient.getInstance().blockRenderer.modelRenderer.renderModel(matrices.last(), vertexConsumer, null, bakedModel, red, green, blue, light, overlay)
            }
        }
        blockEntity.render(renderingProxy, tickDelta, light, overlay)
    }
}
