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
        setSize(0.01F, 0.01F)
        gravity = 0.06F
        setColor(255 / 255F, 159 / 255F, 50 / 255F)
    }

    override fun getRenderType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun tick() {
        xo = x
        yo = y
        zo = z

        lifetime--
        if (lifetime <= 0) {
            remove()
            onDiedByAge()
        }
        if (removed) return

        yd -= gravity
        move(xd, yd, zd)
        updateVelocity()
        if (removed) return

        xd *= 0.98F
        yd *= 0.98F
        zd *= 0.98F
    }

    open fun onDiedByAge() {

    }

    open fun updateVelocity() {

    }

    class Dripping(world: ClientWorld, x: Double, y: Double, z: Double, spriteProvider: SpriteProvider, private val particleEffect: ParticleEffect) : HaimeviskaSapParticle(world, x, y, z) {
        init {
            pickSprite(spriteProvider)
            gravity *= 0.01F
            lifetime = 100
        }

        override fun onDiedByAge() {
            level.addParticle(particleEffect, x, y, z, xd, yd, zd)
        }

        override fun updateVelocity() {
            xd *= 0.02
            yd *= 0.02
            zd *= 0.02
        }
    }

    class Falling(world: ClientWorld, x: Double, y: Double, z: Double, spriteProvider: SpriteProvider, private val particleEffect: ParticleEffect) : HaimeviskaSapParticle(world, x, y, z) {
        init {
            pickSprite(spriteProvider)
            gravity = 0.01F
            lifetime = (64.0 / (world.random.nextDouble() * 0.8 + 0.2)).toInt()
        }

        override fun updateVelocity() {
            if (onGround) {
                remove()
                level.addParticle(particleEffect, x, y, z, 0.0, 0.0, 0.0)
                level.playLocalSound(x, y, z, SoundEvents.BEEHIVE_DRIP, SoundCategory.BLOCKS, 0.3F + 0.7F * level.random.nextFloat(), 1.0F, false)
            }
        }
    }

    class Landing(world: ClientWorld, x: Double, y: Double, z: Double, spriteProvider: SpriteProvider) : HaimeviskaSapParticle(world, x, y, z) {
        init {
            pickSprite(spriteProvider)
            lifetime = (128.0 / (world.random.nextDouble() * 0.8 + 0.2)).toInt()
        }
    }
}
