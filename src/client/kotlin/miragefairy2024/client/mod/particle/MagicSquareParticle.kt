package miragefairy2024.client.mod.particle

import miragefairy2024.mod.MagicSquareParticleEffect
import mirrg.kotlin.hydrogen.max
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
    ParticleFactory<MagicSquareParticleEffect> { parameters, world, x, y, z, _, _, _ ->
        MagicSquareParticle(world, x, y, z, parameters.layer, parameters.targetPosition, spriteProvider)
    }
}

class MagicSquareParticle(world: ClientWorld, x: Double, y: Double, z: Double, layer: Int, private val targetPosition: Vec3d, spriteProvider: SpriteProvider) : SpriteBillboardParticle(world, x, y, z) {

    var delay = 0
    var alphaTicks = arrayOf(0F, 20F, 40F, 60F)
    var lightTicks = arrayOf(0F, 20F, 40F, 60F)

    init {
        val sprites = (spriteProvider as FabricSpriteProvider).sprites
        setSprite(sprites[layer.coerceIn(sprites.indices)])
        maxAge = 60
        scale = 0.5F
    }

    private fun getValue(tickDelta: Float, ticks: Array<Float>): Float {
        val a = age.toFloat() + tickDelta
        return when {
            a < ticks[0] -> 0F
            a < ticks[1] -> 0F + (a - ticks[0]) / (ticks[1] - ticks[0])
            a < ticks[2] -> 1F
            a < ticks[3] -> 1F - (a - ticks[2]) / (ticks[3] - ticks[2])
            else -> 0F
        }
    }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        if (delay > 0) return
        alpha = getValue(tickDelta, alphaTicks)
        buildGeometry(vertexConsumer, camera, tickDelta, false)
        buildGeometry(vertexConsumer, camera, tickDelta, true)
    }

    private fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float, flip: Boolean) {

        val offsetX = (targetPosition.x - x).toFloat()
        val offsetY = (targetPosition.y - y).toFloat()
        val offsetZ = (targetPosition.z - z).toFloat()

        val yaw = MathHelper.atan2(offsetX.toDouble(), offsetZ.toDouble()).toFloat()
        val pitch = MathHelper.atan2(-offsetY.toDouble(), MathHelper.sqrt(offsetX * offsetX + offsetZ * offsetZ).toDouble()).toFloat()

        val quaternionf = Quaternionf().rotationYXZ(yaw, pitch, MathHelper.lerp(tickDelta, prevAngle, angle))
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
        if (delay > 0) {
            delay--
            return
        }

        age++
        if (age >= maxAge) markDead()

        prevAngle = angle
        angle += MathHelper.TAU / 60F
    }

    override fun getSize(tickDelta: Float) = scale * (1F + 0.2F * MathHelper.sin((age.toFloat() + tickDelta) / 40F))

    override fun getBrightness(tint: Float): Int {
        val brightness = super.getBrightness(tint)
        val oldSkyLight = (brightness shr 20) and 0xF
        val oldBlockLight = (brightness shr 4) and 0xF
        val skyLight = oldSkyLight
        val blockLight = oldBlockLight max (15F * getValue(tint, lightTicks)).roundToInt()
        return (skyLight shl 20) or (blockLight shl 4)
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT

}
