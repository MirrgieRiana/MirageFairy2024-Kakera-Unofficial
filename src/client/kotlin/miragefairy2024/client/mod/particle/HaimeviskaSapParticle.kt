package miragefairy2024.client.mod.particle

import net.minecraft.client.particle.ParticleRenderType as ParticleTextureSheet
import net.minecraft.client.particle.TextureSheetParticle as SpriteBillboardParticle
import net.minecraft.client.particle.SpriteSet as SpriteProvider
import net.minecraft.client.multiplayer.ClientLevel as ClientWorld
import net.minecraft.core.particles.ParticleOptions as ParticleEffect
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.sounds.SoundEvents

@Suppress("LeakingThis")
open class HaimeviskaSapParticle(world: ClientWorld, x: Double, y: Double, z: Double) : SpriteBillboardParticle(world, x, y, z) {
    init {
        setBoundingBoxSpacing(0.01F, 0.01F)
        gravityStrength = 0.06F
        setColor(255 / 255F, 159 / 255F, 50 / 255F)
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun tick() {
        prevPosX = x
        prevPosY = y
        prevPosZ = z

        maxAge--
        if (maxAge <= 0) {
            markDead()
            onDiedByAge()
        }
        if (dead) return

        velocityY -= gravityStrength
        move(velocityX, velocityY, velocityZ)
        updateVelocity()
        if (dead) return

        velocityX *= 0.98F
        velocityY *= 0.98F
        velocityZ *= 0.98F
    }

    open fun onDiedByAge() {

    }

    open fun updateVelocity() {

    }

    class Dripping(world: ClientWorld, x: Double, y: Double, z: Double, spriteProvider: SpriteProvider, private val particleEffect: ParticleEffect) : HaimeviskaSapParticle(world, x, y, z) {
        init {
            setSprite(spriteProvider)
            gravityStrength *= 0.01F
            maxAge = 100
        }

        override fun onDiedByAge() {
            world.addParticle(particleEffect, x, y, z, velocityX, velocityY, velocityZ)
        }

        override fun updateVelocity() {
            velocityX *= 0.02
            velocityY *= 0.02
            velocityZ *= 0.02
        }
    }

    class Falling(world: ClientWorld, x: Double, y: Double, z: Double, spriteProvider: SpriteProvider, private val particleEffect: ParticleEffect) : HaimeviskaSapParticle(world, x, y, z) {
        init {
            setSprite(spriteProvider)
            gravityStrength = 0.01F
            maxAge = (64.0 / (world.random.nextDouble() * 0.8 + 0.2)).toInt()
        }

        override fun updateVelocity() {
            if (onGround) {
                markDead()
                world.addParticle(particleEffect, x, y, z, 0.0, 0.0, 0.0)
                world.playSound(x, y, z, SoundEvents.BLOCK_BEEHIVE_DRIP, SoundCategory.BLOCKS, 0.3F + 0.7F * world.random.nextFloat(), 1.0F, false)
            }
        }
    }

    class Landing(world: ClientWorld, x: Double, y: Double, z: Double, spriteProvider: SpriteProvider) : HaimeviskaSapParticle(world, x, y, z) {
        init {
            setSprite(spriteProvider)
            maxAge = (128.0 / (world.random.nextDouble() * 0.8 + 0.2)).toInt()
        }
    }
}
