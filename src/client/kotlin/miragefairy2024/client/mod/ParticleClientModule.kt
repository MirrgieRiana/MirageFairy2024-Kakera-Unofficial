package miragefairy2024.client.mod

import miragefairy2024.mod.ParticleTypeCard
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.client.particle.EnchantGlyphParticle
import net.minecraft.client.particle.EndRodParticle
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.particle.SuspendParticle
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import net.minecraft.particle.ParticleEffect
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper

fun initParticleClientModule() {
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MISSION.particleType, SuspendParticle::HappyVillagerFactory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.COLLECTING_MAGIC.particleType, EnchantGlyphParticle::EnchantFactory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DESCENDING_MAGIC.particleType, EndRodParticle::Factory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MIRAGE_FLOUR.particleType, SuspendParticle::HappyVillagerFactory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.ATTRACTING_MAGIC.particleType, AttractingParticle::Factory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.AURA.particleType) { spriteProvider ->
        val factory = EndRodParticle.Factory(spriteProvider)
        ParticleFactory { parameters, world, x, y, z, velocityX, velocityY, velocityZ ->
            factory.createParticle(parameters, world, x, y, z, velocityX, velocityY, velocityZ)?.also { particle ->
                particle.maxAge = 20 + world.random.nextInt(12)
            }
        }
    }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.CHAOS_STONE.particleType, createRollingFallingParticleFactory(0.0F))
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType, createRollingFallingParticleFactory(1.0F))
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DRIPPING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleFactory { _, world, x, y, z, _, _, _ -> HaimeviskaSapParticle.Dripping(world, x, y, z, spriteProvider, ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleFactory { _, world, x, y, z, _, _, _ -> HaimeviskaSapParticle.Falling(world, x, y, z, spriteProvider, ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleFactory { _, world, x, y, z, _, _, _ -> HaimeviskaSapParticle.Landing(world, x, y, z, spriteProvider) } }
}

class AttractingParticle internal constructor(
    clientWorld: ClientWorld,
    fromX: Double, fromY: Double, fromZ: Double,
    toX: Double, toY: Double, toZ: Double,
) : SpriteBillboardParticle(clientWorld, fromX, fromY, fromZ, 0.0, 0.0, 0.0) {
    private val aX: Double
    private val aY: Double
    private val aZ: Double

    init {
        velocityX = 0.0
        velocityY = 0.0
        velocityZ = 0.0
        maxAge = 40
        aX = (toX - fromX) / (maxAge * (maxAge + 1) / 2.0)
        aY = (toY - fromY) / (maxAge * (maxAge + 1) / 2.0)
        aZ = (toZ - fromZ) / (maxAge * (maxAge + 1) / 2.0)
        scale *= 0.3F
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun move(dx: Double, dy: Double, dz: Double) {
        boundingBox = boundingBox.offset(dx, dy, dz)
        repositionFromBoundingBox()
    }

    override fun tick() {
        velocityX += aX
        velocityY += aY
        velocityZ += aZ
        super.tick()
    }

    class Factory(private val spriteProvider: SpriteProvider) : ParticleFactory<DefaultParticleType> {
        override fun createParticle(defaultParticleType: DefaultParticleType, clientWorld: ClientWorld, d: Double, e: Double, f: Double, g: Double, h: Double, i: Double): Particle {
            val particle = AttractingParticle(clientWorld, d, e, f, g, h, i)
            particle.setSprite(spriteProvider)
            return particle
        }
    }
}

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
