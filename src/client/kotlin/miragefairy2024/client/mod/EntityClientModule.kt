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
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.SinglePartEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import org.joml.Quaternionf
import kotlin.math.atan
import kotlin.math.sqrt

fun initEntityClientModule() {
    EntityModelLayerRegistry.registerModelLayer(AntimatterBoltEntityRenderer.MAIN.entityModelLayer, AntimatterBoltEntityRenderer.MAIN.provider)
    EntityRendererRegistry.register(AntimatterBoltCard.entityType, ::AntimatterBoltEntityRenderer)
    EntityModelLayerRegistry.registerModelLayer(ChaosCubeEntityRenderer.ROOT.entityModelLayer, ChaosCubeEntityRenderer.ROOT.provider)
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

class ChaosCubeEntityRenderer(context: EntityRendererFactory.Context) : MobEntityRenderer<ChaosCubeEntity, ChaosCubeEntityModel>(context, ChaosCubeEntityModel(context.getPart(ROOT.entityModelLayer)), 0.5F) {
    companion object {
        val ROOT = EntityModelLayerCard(ChaosCubeCard.identifier, "root", 64, 64) {
            (0..1).forEach { x ->
                (0..1).forEach { y ->
                    (0..1).forEach { z ->
                        it.addChild(
                            "part${x * 4 + y * 2 + z}",
                            ModelPartBuilder.create().uv(0 + x * 20, 0 + y * 20 + (1 - z) * 10).cuboid(0F, 0F, 0F, 5F, 5F, 5F),
                            ModelTransform.pivot(-5.5F + 6F * x, -5.5F + 6F * y, -5.5F + 6F * z),
                        )
                    }
                }
            }
        }
    }

    private val texture = MirageFairy2024.identifier("textures/entity/chaos_cube.png")

    override fun getTexture(entity: ChaosCubeEntity) = texture
}

class ChaosCubeEntityModel(private val root: ModelPart) : EntityModel<ChaosCubeEntity>() {
    companion object {
        private val ROLL = atan(sqrt(2.0F) / 2.0F)
        private val PITCH = 45.0F / 180F * MathHelper.PI
    }

    private val parts = (0 until 8).map { root.getChild("part$it") }.toTypedArray()
    private var segments: Array<ChaosCubeEntity.Segment>? = null
    private val rotations = (0 until 8).map { Quaternionf() }.toTypedArray()

    override fun setAngles(entity: ChaosCubeEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) {
        val delta = animationProgress - entity.age

        val f = animationProgress * 2 * MathHelper.PI / 100
        root.yaw = f

        segments = entity.segments
        repeat(8) { i ->
            rotations[i].x = MathHelper.lerp(delta, entity.segments[i].prevRotation.x, entity.segments[i].rotation.x)
            rotations[i].y = MathHelper.lerp(delta, entity.segments[i].prevRotation.y, entity.segments[i].rotation.y)
            rotations[i].z = MathHelper.lerp(delta, entity.segments[i].prevRotation.z, entity.segments[i].rotation.z)
            rotations[i].w = MathHelper.lerp(delta, entity.segments[i].prevRotation.w, entity.segments[i].rotation.w)
        }
    }

    override fun render(matrices: MatrixStack, vertices: VertexConsumer, light: Int, overlay: Int, red: Float, green: Float, blue: Float, alpha: Float) {
        if (!root.visible) return
        matrices.stack {
            matrices.translate(0F, 0.5F, 0F)
            root.rotate(matrices)
            matrices.multiply(Quaternionf().rotateZYX(ROLL, 0F, PITCH))

            repeat(8) { i ->
                val segment = segments!![i]
                matrices.stack {
                    matrices.multiply(rotations[i])
                    parts[segment.partIndex].render(matrices, vertices, light, overlay, red, green, blue, alpha)
                }
            }
        }
    }
}
