package miragefairy2024.client.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.client.util.stack
import miragefairy2024.mod.AntimatterBoltCard
import miragefairy2024.mod.AntimatterBoltEntity
import miragefairy2024.mod.ChaosCubeCard
import miragefairy2024.mod.ChaosCubeEntity
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPart
import net.minecraft.client.model.ModelPartBuilder
import net.minecraft.client.model.ModelPartData
import net.minecraft.client.model.ModelTransform
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.SinglePartEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis

fun initEntityClientModule() {
    EntityModelLayerRegistry.registerModelLayer(AntimatterBoltEntityRenderer.MAIN.entityModelLayer, AntimatterBoltEntityRenderer.MAIN.provider)
    EntityRendererRegistry.register(AntimatterBoltCard.entityType, ::AntimatterBoltEntityRenderer)
    EntityModelLayerRegistry.registerModelLayer(ChaosCubeEntityRenderer.MAIN.entityModelLayer, ChaosCubeEntityRenderer.MAIN.provider)
    EntityRendererRegistry.register(ChaosCubeCard.entityType, ::ChaosCubeEntityRenderer)
}

class EntityModelLayerCard(
    identifier: Identifier,
    layerName: String,
    private val textureWidth: Int,
    private val textureHeight: Int,
    private val configurator: (ModelPartData) -> Unit,
) {
    val entityModelLayer = EntityModelLayer(identifier, layerName)
    val provider = EntityModelLayerRegistry.TexturedModelDataProvider {
        val modelData = ModelData()
        configurator(modelData.root)
        TexturedModelData.of(modelData, textureWidth, textureHeight)
    }
}

class AntimatterBoltEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<AntimatterBoltEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(AntimatterBoltCard.identifier, "main", 16, 16) {
            it.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-6F, -1F, -1F, 12F, 2F, 2F), ModelTransform.NONE)
        }
    }

    private val texture = MirageFairy2024.identifier("textures/block/creative_aura_stone.png")

    private val model = object : SinglePartEntityModel<AntimatterBoltEntity>() {
        private val modelPart: ModelPart = context.getPart(MAIN.entityModelLayer)
        override fun setAngles(entity: AntimatterBoltEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) = Unit
        override fun getPart() = modelPart
    }

    override fun getTexture(entity: AntimatterBoltEntity) = texture

    override fun render(entity: AntimatterBoltEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        matrices.stack {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((MathHelper.lerp(tickDelta, entity.prevYaw, entity.yaw) - 90.0F) / 180F * MathHelper.PI))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(MathHelper.lerp(tickDelta, entity.prevPitch, entity.pitch) / 180F * MathHelper.PI))

            model.setAngles(entity, tickDelta, 0.0F, -0.1F, 0.0F, 0.0F)
            val vertexConsumer = vertexConsumers.getBuffer(model.getLayer(texture))
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F)
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }
}

class ChaosCubeEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<ChaosCubeEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(ChaosCubeCard.identifier, "main", 16, 16) {
            it.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-5F, 0F, -5F, 10F, 24F, 10F), ModelTransform.NONE)
        }
    }

    private val texture = MirageFairy2024.identifier("textures/block/miranagite_block.png")

    private val model = object : SinglePartEntityModel<ChaosCubeEntity>() {
        private val modelPart: ModelPart = context.getPart(MAIN.entityModelLayer)
        override fun setAngles(entity: ChaosCubeEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) = Unit
        override fun getPart() = modelPart
    }

    override fun getTexture(entity: ChaosCubeEntity) = texture

    override fun render(entity: ChaosCubeEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        matrices.stack {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation((MathHelper.lerp(tickDelta, entity.prevYaw, entity.yaw) - 90.0F) / 180F * MathHelper.PI))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(MathHelper.lerp(tickDelta, entity.prevPitch, entity.pitch) / 180F * MathHelper.PI))

            model.setAngles(entity, tickDelta, 0.0F, -0.1F, 0.0F, 0.0F)
            val vertexConsumer = vertexConsumers.getBuffer(model.getLayer(texture))
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F)
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }
}
