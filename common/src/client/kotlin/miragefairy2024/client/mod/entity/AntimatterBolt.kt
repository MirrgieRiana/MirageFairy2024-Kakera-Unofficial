package miragefairy2024.client.mod.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.AntimatterBoltCard
import miragefairy2024.mod.entity.AntimatterBoltEntity
import miragefairy2024.util.times
import net.minecraft.client.model.HierarchicalModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth

class AntimatterBoltEntityRenderer(context: EntityRendererProvider.Context) : EntityRenderer<AntimatterBoltEntity>(context) {
    companion object {
        val MAIN = EntityModelLayerCard(AntimatterBoltCard.identifier, "main", 16, 16) {
            it.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-6F, -1F, -1F, 12F, 2F, 2F), PartPose.ZERO)
        }
    }

    private val texture = "textures/entity/" * AntimatterBoltCard.identifier * ".png"

    private val model = object : HierarchicalModel<AntimatterBoltEntity>() {
        private val modelPart: ModelPart = context.bakeLayer(MAIN.entityModelLayer)
        override fun setupAnim(entity: AntimatterBoltEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) = Unit
        override fun root() = modelPart
    }

    override fun getTextureLocation(entity: AntimatterBoltEntity) = texture

    override fun render(entity: AntimatterBoltEntity, yaw: Float, tickDelta: Float, matrices: PoseStack, vertexConsumers: MultiBufferSource, light: Int) {
        matrices.stack {
            matrices.mulPose(Axis.YP.rotation((Mth.lerp(tickDelta, entity.yRotO, entity.yRot) - 90.0F) / 180F * Mth.PI))
            matrices.mulPose(Axis.ZP.rotation(Mth.lerp(tickDelta, entity.xRotO, entity.xRot) / 180F * Mth.PI))

            model.setupAnim(entity, tickDelta, 0.0F, -0.1F, 0.0F, 0.0F)
            val vertexConsumer = vertexConsumers.getBuffer(model.renderType(texture))
            model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF.toInt())
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
    }
}
