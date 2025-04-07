package miragefairy2024.client.mod.particle

import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.math.MathHelper

fun createRollingFallingParticleFactory(velocityAngleFactor: Float) = { spriteProvider: SpriteProvider ->
    ParticleFactory<DefaultParticleType> { _, world, x, y, z, _, _, _ ->
        object : SpriteBillboardParticle(world, x, y, z) {

            private var velocityAngle = 0F

            init {
                scale *= 0.675F
                maxAge = (32.0 / (0.2 + 0.8 * Math.random()) * 0.9).toInt() atLeast 1
                angle = if (velocityAngleFactor != 0F) Math.random().toFloat() * MathHelper.PI * 2F else 0F
                velocityAngle = MathHelper.PI * 0.1F * (Math.random().toFloat() * 2F - 1F) * velocityAngleFactor
                setSpriteForAge(spriteProvider)
            }

            override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE
            override fun getSize(tickDelta: Float): Float {
                val actualAge = age.toFloat() + tickDelta
                val lifeRate = actualAge / maxAge.toFloat()

                if (lifeRate < 1F / 32F) return scale * (lifeRate * 32F atLeast 0F atMost 1F)
                if (lifeRate > 24F / 32F) return scale * ((1F - lifeRate) * 4F atLeast 0F atMost 1F)
                return scale * (32F * lifeRate atLeast 0F atMost 1F)
            }

            override fun tick() {
                prevPosX = this.x
                prevPosY = this.y
                prevPosZ = this.z
                prevAngle = angle

                age++
                if (age >= maxAge) {
                    markDead()
                    return
                }

                setSpriteForAge(spriteProvider)

                move(velocityX, velocityY, velocityZ)
                angle += velocityAngle

                velocityY = velocityY - 0.003 atLeast -0.14
                if (onGround) velocityAngle = 0F

            }
        }
    }
}
