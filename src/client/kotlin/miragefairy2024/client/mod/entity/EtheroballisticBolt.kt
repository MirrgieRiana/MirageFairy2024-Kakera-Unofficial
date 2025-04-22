package miragefairy2024.client.mod.entity

import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.EtheroballisticBoltCard
import miragefairy2024.mod.entity.EtheroballisticBoltEntity
import miragefairy2024.util.times
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import com.mojang.blaze3d.vertex.PoseStack as MatrixStack
import com.mojang.math.Axis as RotationAxis
import net.minecraft.client.model.HierarchicalModel as SinglePartEntityModel
import net.minecraft.client.model.geom.PartPose as ModelTransform
import net.minecraft.client.model.geom.builders.CubeListBuilder as ModelPartBuilder
import net.minecraft.client.renderer.MultiBufferSource as VertexConsumerProvider
import net.minecraft.client.renderer.entity.EntityRendererProvider as EntityRendererFactory
import net.minecraft.util.Mth as MathHelper

class EtheroballisticBoltEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<EtheroballisticBoltEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(EtheroballisticBoltCard.identifier, "main", 16, 16) {
            it.addOrReplaceChild("main", ModelPartBuilder.create().texOffs(0, 0).addBox(-1F, -6F, -1F, 2F, 12F, 2F), ModelTransform.ZERO)
        }
    }

    private val texture = "textures/entity/" * EtheroballisticBoltCard.identifier * ".png"

    private val model = object : SinglePartEntityModel<EtheroballisticBoltEntity>() {
        private val modelPart: ModelPart = context.bakeLayer(MAIN.entityModelLayer)
        override fun setupAnim(entity: EtheroballisticBoltEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) = Unit
        override fun root() = modelPart
    }

    override fun getTextureLocation(entity: EtheroballisticBoltEntity) = texture

    override fun render(entity: EtheroballisticBoltEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        matrices.stack {
            matrices.mulPose(RotationAxis.YP.rotation((MathHelper.lerp(tickDelta, entity.yRotO, entity.yRot) - 90.0F) / 180F * MathHelper.PI))
            matrices.mulPose(RotationAxis.ZP.rotation((MathHelper.lerp(tickDelta, entity.xRotO, entity.xRot) / 180F - 0.5F) * MathHelper.PI))

            model.setupAnim(entity, tickDelta, 0.0F, -0.1F, 0.0F, 0.0F)
            val vertexConsumer = vertexConsumers.getBuffer(model.renderType(texture))
            model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F)
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }
}
