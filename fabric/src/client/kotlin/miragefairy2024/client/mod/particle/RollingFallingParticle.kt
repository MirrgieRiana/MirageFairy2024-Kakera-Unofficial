package miragefairy2024.client.mod.particle

import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.client.particle.ParticleProvider as ParticleFactory
import net.minecraft.client.particle.ParticleRenderType as ParticleTextureSheet
import net.minecraft.client.particle.SpriteSet as SpriteProvider
import net.minecraft.client.particle.TextureSheetParticle as SpriteBillboardParticle
import net.minecraft.core.particles.SimpleParticleType as DefaultParticleType
import net.minecraft.util.Mth as MathHelper

fun createRollingFallingParticleFactory(velocityAngleFactor: Float) = { spriteProvider: SpriteProvider ->
    ParticleFactory<DefaultParticleType> { _, world, x, y, z, _, _, _ ->
        object : SpriteBillboardParticle(world, x, y, z) {

            private var velocityAngle = 0F

            init {
                quadSize *= 0.675F
                lifetime = (32.0 / (0.2 + 0.8 * Math.random()) * 0.9).toInt() atLeast 1
                roll = if (velocityAngleFactor != 0F) Math.random().toFloat() * MathHelper.PI * 2F else 0F
                velocityAngle = MathHelper.PI * 0.1F * (Math.random().toFloat() * 2F - 1F) * velocityAngleFactor
                setSpriteFromAge(spriteProvider)
            }

            override fun getRenderType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE
            override fun getQuadSize(tickDelta: Float): Float {
                val actualAge = age.toFloat() + tickDelta
                val lifeRate = actualAge / lifetime.toFloat()

                if (lifeRate < 1F / 32F) return quadSize * (lifeRate * 32F atLeast 0F atMost 1F)
                if (lifeRate > 24F / 32F) return quadSize * ((1F - lifeRate) * 4F atLeast 0F atMost 1F)
                return quadSize * (32F * lifeRate atLeast 0F atMost 1F)
            }

            override fun tick() {
                xo = this.x
                yo = this.y
                zo = this.z
                oRoll = roll

                age++
                if (age >= lifetime) {
                    remove()
                    return
                }

                setSpriteFromAge(spriteProvider)

                move(xd, yd, zd)
                roll += velocityAngle

                yd = yd - 0.003 atLeast -0.14
                if (onGround) velocityAngle = 0F

            }
        }
    }
}
