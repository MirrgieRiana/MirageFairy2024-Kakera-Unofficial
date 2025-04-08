package miragefairy2024.client.mod.particle

import miragefairy2024.mod.MagicSquareParticleEffect
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

fun createMagicSquareParticleFactory() = { spriteProvider: SpriteProvider ->
    ParticleFactory<MagicSquareParticleEffect> { parameters, world, x, y, z, velocityX, _, _ ->
        MagicSquareParticle(world, x, y, z, velocityX.roundToInt(), parameters.targetPosition, spriteProvider)
    }
}

class MagicSquareParticle(world: ClientWorld, x: Double, y: Double, z: Double, index: Int, private val targetPosition: Vec3d, spriteProvider: SpriteProvider) : SpriteBillboardParticle(world, x, y, z) {

    var delay = 0

    init {
        setSprite((spriteProvider as FabricSpriteProvider).sprites[index])
        maxAge = 60
        scale = 0.5F
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
        buildGeometry(vertexConsumer, camera, tickDelta, false)
        buildGeometry(vertexConsumer, camera, tickDelta, true)
    }

    private fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float, flip: Boolean) {


        val offsetX = (targetPosition.x - x).toFloat()
        val offsetY = (targetPosition.y - y).toFloat()
        val offsetZ = (targetPosition.z - z).toFloat()

        val yaw = MathHelper.atan2(offsetX.toDouble(), offsetZ.toDouble()).toFloat()
        val pitch = MathHelper.atan2(-offsetY.toDouble(), MathHelper.sqrt(offsetX * offsetX + offsetZ * offsetZ).toDouble()).toFloat()


        val quaternionf = Quaternionf().setAngleAxis(0F, 1F, 1F, 1F)
        quaternionf.rotateY(yaw)
        quaternionf.rotateX(pitch)
        //println(targetPosition) // TODO
        //println("$x $y $z") // TODO
        //println(Vector3f((targetPosition.x - x).toFloat(), (targetPosition.y - y).toFloat(), (targetPosition.z - z).toFloat())) // TODO
        quaternionf.rotateZ(MathHelper.lerp(tickDelta, prevAngle, angle))
        if (flip) quaternionf.rotateY(-MathHelper.PI)

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
