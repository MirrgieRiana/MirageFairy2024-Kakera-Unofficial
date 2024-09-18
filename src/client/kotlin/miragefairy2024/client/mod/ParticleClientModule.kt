package miragefairy2024.client.mod

import miragefairy2024.mod.ParticleTypeCard
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

fun initParticleClientModule() {
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MISSION.particleType, SuspendParticle::HappyVillagerFactory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.COLLECTING_MAGIC.particleType, EnchantGlyphParticle::EnchantFactory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DESCENDING_MAGIC.particleType, EndRodParticle::Factory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MIRAGE_FLOUR.particleType, SuspendParticle::HappyVillagerFactory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.ATTRACTING_MAGIC.particleType, AttractingParticle::Factory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.AURA.particleType, EndRodParticle::Factory)
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
