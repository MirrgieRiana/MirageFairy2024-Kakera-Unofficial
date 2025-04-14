package miragefairy2024.client.mod.particle

import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.particle.MagicSquareParticleChannel
import miragefairy2024.mod.particle.MagicSquareParticleEffect
import mirrg.kotlin.hydrogen.max
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.particle.ParticleProvider as ParticleFactory
import net.minecraft.client.particle.ParticleRenderType as ParticleTextureSheet
import net.minecraft.client.particle.TextureSheetParticle as SpriteBillboardParticle
import net.minecraft.client.particle.SpriteSet as SpriteProvider
import net.minecraft.client.Camera
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.multiplayer.ClientLevel as ClientWorld
import net.minecraft.util.Mth as MathHelper
import net.minecraft.world.phys.Vec3 as Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

fun initMagicSquareParticle() {
    MagicSquareParticleChannel.registerClientPacketReceiver { packet ->
        val particleManager = MinecraftClient.getInstance()?.particleManager ?: return@registerClientPacketReceiver
        (0..6).forEach { i ->
            particleManager.addParticle(
                MagicSquareParticleEffect(i, packet.targetPosition, 30F * (i.toFloat() / 6F)),
                packet.position.x,
                packet.position.y,
                packet.position.z,
                0.0,
                0.0,
                0.0,
            )
        }
    }
}

fun createMagicSquareParticleFactory() = { spriteProvider: SpriteProvider ->
    ParticleFactory<MagicSquareParticleEffect> { parameters, world, x, y, z, _, _, _ ->
        MagicSquareParticle(world, x, y, z, parameters.layer, parameters.targetPosition, spriteProvider).also {
            it.alphaTicks[0] = parameters.delay
            it.alphaTicks[1] = parameters.delay + 20F
            it.lightTicks[0] = parameters.delay
            it.lightTicks[1] = parameters.delay + 20F
        }
    }
}

class MagicSquareParticle(world: ClientWorld, x: Double, y: Double, z: Double, layer: Int, private val targetPosition: Vec3d, spriteProvider: SpriteProvider) : SpriteBillboardParticle(world, x, y, z) {

    var delay = 0
    var color1 = 0xFFF4D3
    var color2 = 0xFFB82C
    var color3 = 0x341B0E
    var alphaTicks = arrayOf(0F, 10F, 70F, 80F)
    var lightTicks = arrayOf(0F, 10F, 70F, 80F)

    init {
        val sprites = (spriteProvider as FabricSpriteProvider).sprites
        setSprite(sprites[layer.coerceIn(sprites.indices)])
        maxAge = 80
        scale = 0.5F
    }

    //       0     1         2     3
    // 　　　　　／￣￣￣￣￣＼
    // 　　　　／　　　　　　　＼
    // ＿＿＿／　　　　　　　　　＼

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

    private fun setColor(color1: Int, color2: Int, delta: Float) {
        val r1 = ((color1 shr 16) and 0xFF).toFloat() / 255F
        val g1 = ((color1 shr 8) and 0xFF).toFloat() / 255F
        val b1 = (color1 and 0xFF).toFloat() / 255F
        val r2 = ((color2 shr 16) and 0xFF).toFloat() / 255F
        val g2 = ((color2 shr 8) and 0xFF).toFloat() / 255F
        val b2 = (color2 and 0xFF).toFloat() / 255F
        red = r1 + (r2 - r1) * delta
        green = g1 + (g2 - g1) * delta
        blue = b1 + (b2 - b1) * delta
    }

    private fun setColor(color: Int) {
        red = ((color shr 16) and 0xFF).toFloat() / 255F
        green = ((color shr 8) and 0xFF).toFloat() / 255F
        blue = (color and 0xFF).toFloat() / 255F
    }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        if (delay > 0) return

        val a = age.toFloat() + tickDelta
        when {
            a < alphaTicks[0] -> setColor(color1)
            a < alphaTicks[1] -> setColor(color1)
            a < alphaTicks[2] -> setColor(color1, color2, (a - alphaTicks[1]) / (alphaTicks[2] - alphaTicks[1]))
            a < alphaTicks[3] -> setColor(color2, color3, (a - alphaTicks[2]) / (alphaTicks[3] - alphaTicks[2]))
            else -> setColor(color3)
        }

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
        angle += MathHelper.TWO_PI / 60F

        // TODO 魔方陣の周りのパーティクル
    }

    override fun getSize(tickDelta: Float) = scale * (1F - 0.2F * MathHelper.cos((age.toFloat() + tickDelta) / 80F * MathHelper.TWO_PI))

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
