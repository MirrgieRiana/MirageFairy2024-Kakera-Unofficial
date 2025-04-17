package miragefairy2024.client.mod.entity

import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.EtheroballisticBoltCard
import miragefairy2024.mod.entity.EtheroballisticBoltEntity
import miragefairy2024.util.times
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.builders.CubeListBuilder as ModelPartBuilder
import net.minecraft.client.model.geom.PartPose as ModelTransform
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.MultiBufferSource as VertexConsumerProvider
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider as EntityRendererFactory
import net.minecraft.client.model.HierarchicalModel as SinglePartEntityModel
import com.mojang.blaze3d.vertex.PoseStack as MatrixStack
import net.minecraft.util.Mth as MathHelper
import com.mojang.math.Axis as RotationAxis

class EtheroballisticBoltEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<EtheroballisticBoltEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(EtheroballisticBoltCard.identifier, "main", 16, 16) {
            it.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-1F, -6F, -1F, 2F, 12F, 2F), ModelTransform.NONE)
        }
    }

    private val texture = "textures/entity/" * EtheroballisticBoltCard.identifier * ".png"

    private val model = object : SinglePartEntityModel<EtheroballisticBoltEntity>() {
        private val modelPart: ModelPart = context.getPart(MAIN.entityModelLayer)
        override fun setAngles(entity: EtheroballisticBoltEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) = Unit
        override fun getPart() = modelPart
    }

    override fun getTextureLocation(entity: EtheroballisticBoltEntity) = texture

    override fun render(entity: EtheroballisticBoltEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        matrices.stack {
            matrices.mulPose(RotationAxis.POSITIVE_Y.rotation((MathHelper.lerp(tickDelta, entity.yRotO, entity.yRot) - 90.0F) / 180F * MathHelper.PI))
            matrices.mulPose(RotationAxis.POSITIVE_Z.rotation((MathHelper.lerp(tickDelta, entity.xRotO, entity.xRot) / 180F - 0.5F) * MathHelper.PI))

            model.setAngles(entity, tickDelta, 0.0F, -0.1F, 0.0F, 0.0F)
            val vertexConsumer = vertexConsumers.getBuffer(model.getLayer(texture))
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F)
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }
}
