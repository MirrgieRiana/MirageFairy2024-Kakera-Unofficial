package miragefairy2024.client

import miragefairy2024.BlockColorProvider
import miragefairy2024.ClientProxy
import miragefairy2024.ItemColorProvider
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.client.util.stack
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LidOpenable
import net.minecraft.client.MinecraftClient
import net.minecraft.client.color.world.BiomeColors
import net.minecraft.client.color.world.FoliageColors
import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPartBuilder
import net.minecraft.client.model.ModelTransform
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
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
                matrices.push()
                try {
                    block()
                } finally {
                    matrices.pop()
                }
            }

            override fun translate(x: Double, y: Double, z: Double) = matrices.translate(x, y, z)
            override fun scale(x: Float, y: Float, z: Float) = matrices.scale(x, y, z)
            override fun rotateX(rad: Float) = matrices.multiply(RotationAxis.POSITIVE_X.rotation(rad))
            override fun rotateY(rad: Float) = matrices.multiply(RotationAxis.POSITIVE_Y.rotation(rad))
            override fun rotateZ(rad: Float) = matrices.multiply(RotationAxis.POSITIVE_Z.rotation(rad))

            override fun renderItemStack(itemStack: ItemStack) {
                MinecraftClient.getInstance().itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, blockEntity.world, 0)
            }

            override fun renderCutoutBlock(identifier: Identifier, variant: String?, red: Float, green: Float, blue: Float, light: Int, overlay: Int) {
                val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getCutout())
                val bakedModel = if (variant != null) {
                    MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(identifier, variant))
                } else {
                    MinecraftClient.getInstance().bakedModelManager.getModel(identifier)
                }
                MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(matrices.peek(), vertexConsumer, null, bakedModel, red, green, blue, light, overlay)
            }
        }
        blockEntity.render(renderingProxy, tickDelta, light, overlay)
    }
}

class ChestBlockEntityRenderer<T>(ctx: BlockEntityRendererFactory.Context) : BlockEntityRenderer<T> where T : BlockEntity, T : LidOpenable {
    companion object {
        val BASE = "bottom"
        val LID = "lid"
        val LATCH = "lock"
        fun getSingleTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            val modelPartData = modelData.root
            modelPartData.addChild(BASE, ModelPartBuilder.create().uv(0, 19).cuboid(1.0f, 0.0f, 1.0f, 14.0f, 10.0f, 14.0f), ModelTransform.NONE)
            modelPartData.addChild(LID, ModelPartBuilder.create().uv(0, 0).cuboid(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f))
            modelPartData.addChild(LATCH, ModelPartBuilder.create().uv(0, 0).cuboid(7.0f, -2.0f, 14.0f, 2.0f, 4.0f, 1.0f), ModelTransform.pivot(0.0f, 9.0f, 1.0f))
            return TexturedModelData.of(modelData, 64, 64)
        }
    }

    private val modelPart = ctx.getLayerModelPart(EntityModelLayers.CHEST)
    private val base = modelPart.getChild(BASE)
    private val lid = modelPart.getChild(LID)
    private val latch = modelPart.getChild(LATCH)

    override fun render(entity: T, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val blockState = if (entity.world != null) entity.cachedState else Blocks.CHEST.defaultState.with(ChestBlock.FACING, Direction.SOUTH)
        matrices.stack {
            val rotation = blockState.get(ChestBlock.FACING).asRotation()
            matrices.translate(0.5F, 0.5F, 0.5F)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation))
            matrices.translate(-0.5F, -0.5F, -0.5F)

            val b = 1.0F - entity.getAnimationProgress(tickDelta)
            val openFactor = 1.0F - b * b * b
            val pitch = -openFactor * MathHelper.HALF_PI
            lid.pitch = pitch
            latch.pitch = pitch

            val vertexConsumer = TexturedRenderLayers.NORMAL.getVertexConsumer(vertexConsumers) { RenderLayer.getEntityCutout(it) }
            lid.render(matrices, vertexConsumer, light, overlay)
            latch.render(matrices, vertexConsumer, light, overlay)
            base.render(matrices, vertexConsumer, light, overlay)
        }
    }
}
