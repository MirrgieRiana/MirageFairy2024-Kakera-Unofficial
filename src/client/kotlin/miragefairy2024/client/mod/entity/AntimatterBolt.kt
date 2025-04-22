package miragefairy2024.client.mod.entity

import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.AntimatterBoltEntity
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

class AntimatterBoltEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<AntimatterBoltEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(AntimatterBoltCard.identifier, "main", 16, 16) {
            it.addOrReplaceChild("main", ModelPartBuilder.create().texOffs(0, 0).addBox(-6F, -1F, -1F, 12F, 2F, 2F), ModelTransform.ZERO)
        }
    }

    private val texture = "textures/entity/" * AntimatterBoltCard.identifier

    private val model = object : SinglePartEntityModel<AntimatterBoltEntity>() {
        private val modelPart: ModelPart = context.bakeLayer(MAIN.entityModelLayer)
        override fun setupAnim(entity: AntimatterBoltEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) = Unit
        override fun root() = modelPart
    }

    override fun getTextureLocation(entity: AntimatterBoltEntity) = texture

    override fun render(entity: AntimatterBoltEntity, yaw: Float, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {
        matrices.stack {
            matrices.mulPose(RotationAxis.YP.rotation((MathHelper.lerp(tickDelta, entity.yRotO, entity.yRot) - 90.0F) / 180F * MathHelper.PI))
            matrices.mulPose(RotationAxis.ZP.rotation(MathHelper.lerp(tickDelta, entity.xRotO, entity.xRot) / 180F * MathHelper.PI))

            model.setupAnim(entity, tickDelta, 0.0F, -0.1F, 0.0F, 0.0F)
            val vertexConsumer = vertexConsumers.getBuffer(model.renderType(texture))
            model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F)
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }
}
