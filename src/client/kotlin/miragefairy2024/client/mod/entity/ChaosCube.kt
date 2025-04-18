package miragefairy2024.client.mod.entity

import miragefairy2024.client.util.stack
import miragefairy2024.mod.entity.ChaosCubeCard
import miragefairy2024.mod.entity.ChaosCubeEntity
import miragefairy2024.util.times
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.builders.CubeListBuilder as ModelPartBuilder
import net.minecraft.client.model.geom.PartPose as ModelTransform
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.entity.EntityRendererProvider as EntityRendererFactory
import net.minecraft.client.renderer.entity.MobRenderer as MobEntityRenderer
import net.minecraft.client.model.EntityModel
import com.mojang.blaze3d.vertex.PoseStack as MatrixStack
import net.minecraft.util.Mth as MathHelper
import org.joml.Quaternionf
import kotlin.math.atan
import kotlin.math.sqrt

class ChaosCubeEntityRenderer(context: EntityRendererFactory.Context) : MobEntityRenderer<ChaosCubeEntity, ChaosCubeEntityModel>(context, ChaosCubeEntityModel(context.bakeLayer(ROOT.entityModelLayer)), 0.5F) {
    companion object {
        val ROOT = EntityModelLayerCard(ChaosCubeCard.identifier, "root", 64, 64) {
            (0..1).forEach { x ->
                (0..1).forEach { y ->
                    (0..1).forEach { z ->
                        it.addOrReplaceChild(
                            "part${x * 4 + y * 2 + z}",
                            ModelPartBuilder.create().texOffs(0 + x * 20, 0 + y * 20 + (1 - z) * 10).addBox(0F, 0F, 0F, 5F, 5F, 5F),
                            ModelTransform.offset(-5.5F + 6F * x, -5.5F + 6F * y, -5.5F + 6F * z),
                        )
                    }
                }
            }
        }
    }

    private val texture = "textures/entity/" * ChaosCubeCard.identifier * ".png"

    override fun getTextureLocation(entity: ChaosCubeEntity) = texture
}

class ChaosCubeEntityModel(private val root: ModelPart) : EntityModel<ChaosCubeEntity>() {
    companion object {
        private val ROLL = atan(sqrt(2.0F) / 2.0F)
        private val PITCH = 45.0F / 180F * MathHelper.PI
    }

    private val parts = (0 until 8).map { root.getChild("part$it") }.toTypedArray()
    private var segments: Array<ChaosCubeEntity.Segment>? = null
    private val rotations = (0 until 8).map { Quaternionf() }.toTypedArray()

    override fun setupAnim(entity: ChaosCubeEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) {
        val delta = animationProgress - entity.tickCount

        val f = animationProgress * 2 * MathHelper.PI / 100
        root.yRot = f

        segments = entity.segments
        repeat(8) { i ->
            rotations[i].x = MathHelper.lerp(delta, entity.segments[i].prevRotation.x, entity.segments[i].rotation.x)
            rotations[i].y = MathHelper.lerp(delta, entity.segments[i].prevRotation.y, entity.segments[i].rotation.y)
            rotations[i].z = MathHelper.lerp(delta, entity.segments[i].prevRotation.z, entity.segments[i].rotation.z)
            rotations[i].w = MathHelper.lerp(delta, entity.segments[i].prevRotation.w, entity.segments[i].rotation.w)
        }
    }

    override fun renderToBuffer(matrices: MatrixStack, vertices: VertexConsumer, light: Int, overlay: Int, red: Float, green: Float, blue: Float, alpha: Float) {
        if (!root.visible) return
        matrices.stack {
            matrices.translate(0F, 0.5F, 0F)
            root.translateAndRotate(matrices)
            matrices.mulPose(Quaternionf().rotateZYX(ROLL, 0F, PITCH))

            repeat(8) { i ->
                val segment = segments!![i]
                matrices.stack {
                    matrices.mulPose(rotations[i])
                    parts[segment.partIndex].render(matrices, vertices, light, overlay, red, green, blue, alpha)
                }
            }
        }
    }
}
