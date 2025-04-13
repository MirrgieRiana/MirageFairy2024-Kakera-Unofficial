package miragefairy2024.client.mod.entity

import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.AntimatterBoltEntity
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

class AntimatterBoltEntityRenderer(context: EntityRendererFactory.Context) : EntityRenderer<AntimatterBoltEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(AntimatterBoltCard.identifier, "main", 16, 16) {
            it.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(-6F, -1F, -1F, 12F, 2F, 2F), ModelTransform.NONE)
        }
    }

    private val texture = "textures/entity/" * AntimatterBoltCard.identifier

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
