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
        private val field_38334: Vector3f = Vector3f(0.5F, 0.5F, 0.5F).normalize()
        private val field_38335: Vector3f = Vector3f(-1.0F, -1.0F, 0.0F)
    }

    init {
        setSprite((spriteProvider as FabricSpriteProvider).sprites[index])
        gravityStrength = 0.0F
        maxAge = 40
        scale = 2.0F
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_LIT

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        super.buildGeometry(vertexConsumer, camera, tickDelta)
    }

    private fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float, rotator: (Quaternionf) -> Unit) {
        val vec3d = camera.pos
        val f = (MathHelper.lerp(tickDelta.toDouble(), prevPosX, x) - vec3d.getX()).toFloat()
        val g = (MathHelper.lerp(tickDelta.toDouble(), prevPosY, y) - vec3d.getY()).toFloat()
        val h = (MathHelper.lerp(tickDelta.toDouble(), prevPosZ, z) - vec3d.getZ()).toFloat()
        val quaternionf = Quaternionf().setAngleAxis(0.0F, field_38334.x(), field_38334.y(), field_38334.z())
        rotator(quaternionf)
        quaternionf.transform(field_38335)
        val vector3fs = arrayOf(
            Vector3f(-1.0F, -1.0F, 0.0F),
            Vector3f(-1.0F, 1.0F, 0.0F),
            Vector3f(1.0F, 1.0F, 0.0F),
            Vector3f(1.0F, -1.0F, 0.0F),
        )
        val i = getSize(tickDelta)

        repeat(4) { j ->
            val vector3f = vector3fs[j]
            vector3f.rotate(quaternionf)
            vector3f.mul(i)
            vector3f.add(f, g, h)
        }

        val j = getBrightness(tickDelta)
        vertex(vertexConsumer, vector3fs[0], maxU, maxV, j)
        vertex(vertexConsumer, vector3fs[1], maxU, minV, j)
        vertex(vertexConsumer, vector3fs[2], minU, minV, j)
        vertex(vertexConsumer, vector3fs[3], minU, maxV, j)
    }

    private fun vertex(vertexConsumer: VertexConsumer, pos: Vector3f, u: Float, v: Float, light: Int) {
        vertexConsumer.vertex(pos.x().toDouble(), pos.y().toDouble(), pos.z().toDouble()).texture(u, v).color(red, green, blue, alpha).light(light).next()
    }

    override fun tick() {
        prevPosX = x
        prevPosY = y
        prevPosZ = z
        age++
        if (age >= maxAge) markDead()
    }
}
