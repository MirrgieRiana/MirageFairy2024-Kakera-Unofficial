package miragefairy2024.client.mod.entity

import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.EtheroballisticBoltCard
import miragefairy2024.mod.entity.EtheroballisticBoltEntity
import miragefairy2024.util.times
import net.minecraft.client.model.ModelPart
import net.minecraft.client.model.ModelPartBuilder
import net.minecraft.client.model.ModelTransform
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.SinglePartEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis

class EtheroballisticBoltEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<EtheroballisticBoltEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(EtheroballisticBoltCard.identifier, "main", 16, 16) {
            it.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-6F, -1F, -1F, 12F, 2F, 2F), ModelTransform.NONE)
        }
    }

    private val texture = "textures/entity/" * EtheroballisticBoltCard.identifier

    private val model = object : SinglePartEntityModel<EtheroballisticBoltEntity>() {
        private val modelPart: ModelPart = context.getPart(MAIN.entityModelLayer)
        override fun setAngles(entity: EtheroballisticBoltEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) = Unit
        override fun getPart() = modelPart
    }

    override fun getTexture(entity: EtheroballisticBoltEntity) = texture

    override fun render(entity: EtheroballisticBoltEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
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
