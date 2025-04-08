package miragefairy2024.client.mod.particle

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.math.MathHelper
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

fun createMagicSquareParticleFactory() = { spriteProvider: SpriteProvider ->
    ParticleFactory<DefaultParticleType> { _, world, x, y, z, velocityX, _, _ ->
        MagicSquareParticle(world, x, y, z, velocityX.roundToInt(), spriteProvider)
    }
}

class MagicSquareParticle(world: ClientWorld, x: Double, y: Double, z: Double, index: Int, spriteProvider: SpriteProvider) : SpriteBillboardParticle(world, x, y, z) {
    companion object {
        private val targetVector: Vector3f = Vector3f(0.5F, 0.5F, 0.5F).normalize()
        private val rotationVector: Vector3f = Vector3f(-1.0F, -1.0F, 0.0F)
    }

    var delay = 0

    init {
        setSprite((spriteProvider as FabricSpriteProvider).sprites[index])
        maxAge = 60
        scale = 1.5F
    }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        if (delay > 0) return
        val a = age.toFloat() + tickDelta
        alpha = when {
            a < 0F -> 0F
            a < 20F -> a / 20F
            a < 40F -> 1F
            a < 60F -> 1F - (a - 40F) / 20F
            else -> 0F
        }
        buildGeometry(vertexConsumer, camera, tickDelta, Quaternionf().rotationX(-1.0472F))
        buildGeometry(vertexConsumer, camera, tickDelta, Quaternionf().rotationYXZ(-Math.PI.toFloat(), 1.0472F, 0.0F))
    }

    private fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float, rotator: Quaternionf) {
        val quaternionf = Quaternionf().setAngleAxis(0.0F, targetVector.x(), targetVector.y(), targetVector.z())
        quaternionf.mul(rotator)
        quaternionf.rotateZ(MathHelper.lerp(tickDelta, prevAngle, angle))
        quaternionf.transform(rotationVector)

        val size = getSize(tickDelta)

        val cameraPos = camera.pos
        val translateX = (MathHelper.lerp(tickDelta.toDouble(), prevPosX, x) - cameraPos.getX()).toFloat()
        val translateY = (MathHelper.lerp(tickDelta.toDouble(), prevPosY, y) - cameraPos.getY()).toFloat()
        val translateZ = (MathHelper.lerp(tickDelta.toDouble(), prevPosZ, z) - cameraPos.getZ()).toFloat()

        val vector3fs = arrayOf(
            Vector3f(-1.0F, -1.0F, 0.0F),
            Vector3f(-1.0F, 1.0F, 0.0F),
            Vector3f(1.0F, 1.0F, 0.0F),
            Vector3f(1.0F, -1.0F, 0.0F),
        )
        vector3fs.forEach { vector3f ->
            vector3f.rotate(quaternionf)
            vector3f.mul(size)
            vector3f.add(translateX, translateY, translateZ)
        }

        val brightness = getBrightness(tickDelta)

        vertex(vertexConsumer, vector3fs[0], maxU, maxV, brightness)
        vertex(vertexConsumer, vector3fs[1], maxU, minV, brightness)
        vertex(vertexConsumer, vector3fs[2], minU, minV, brightness)
        vertex(vertexConsumer, vector3fs[3], minU, maxV, brightness)
    }

    private fun vertex(vertexConsumer: VertexConsumer, pos: Vector3f, u: Float, v: Float, light: Int) {
        vertexConsumer.vertex(pos.x().toDouble(), pos.y().toDouble(), pos.z().toDouble()).texture(u, v).color(red, green, blue, alpha).light(light).next()
    }

    override fun tick() {
        prevAngle = angle

        if (delay > 0) {
            delay--
            return
        }

        age++
        if (age >= maxAge) markDead()

        angle += MathHelper.TAU / 60F
    }

    override fun getSize(tickDelta: Float) = scale * (1F + 0.2F * MathHelper.sin((age.toFloat() + tickDelta) / 40F))

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT

}
