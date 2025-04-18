package miragefairy2024.client.mod.particle

import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider as ParticleFactory
import net.minecraft.client.particle.ParticleRenderType as ParticleTextureSheet
import net.minecraft.client.particle.TextureSheetParticle as SpriteBillboardParticle
import net.minecraft.client.particle.SpriteSet as SpriteProvider
import net.minecraft.client.multiplayer.ClientLevel as ClientWorld
import net.minecraft.core.particles.SimpleParticleType as DefaultParticleType

class AttractingParticle internal constructor(
    clientWorld: ClientWorld,
    fromX: Double, fromY: Double, fromZ: Double,
    toX: Double, toY: Double, toZ: Double,
) : SpriteBillboardParticle(clientWorld, fromX, fromY, fromZ, 0.0, 0.0, 0.0) {
    private val aX: Double
    private val aY: Double
    private val aZ: Double

    init {
        xd = 0.0
        yd = 0.0
        zd = 0.0
        lifetime = 40
        aX = (toX - fromX) / (lifetime * (lifetime + 1) / 2.0)
        aY = (toY - fromY) / (lifetime * (lifetime + 1) / 2.0)
        aZ = (toZ - fromZ) / (lifetime * (lifetime + 1) / 2.0)
        quadSize *= 0.3F
    }

    override fun getRenderType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun move(dx: Double, dy: Double, dz: Double) {
        boundingBox = boundingBox.move(dx, dy, dz)
        setLocationFromBoundingbox()
    }

    override fun tick() {
        xd += aX
        yd += aY
        zd += aZ
        super.tick()
    }

    class Factory(private val spriteProvider: SpriteProvider) : ParticleFactory<DefaultParticleType> {
        override fun createParticle(defaultParticleType: DefaultParticleType, clientWorld: ClientWorld, d: Double, e: Double, f: Double, g: Double, h: Double, i: Double): Particle {
            val particle = AttractingParticle(clientWorld, d, e, f, g, h, i)
            particle.pickSprite(spriteProvider)
            return particle
        }
    }
}
