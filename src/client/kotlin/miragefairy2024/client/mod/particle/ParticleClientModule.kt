package miragefairy2024.client.mod.particle

import miragefairy2024.mod.particle.ParticleTypeCard
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.client.particle.EnchantmentTableParticle as EnchantGlyphParticle
import net.minecraft.client.particle.EndRodParticle
import net.minecraft.client.particle.ParticleProvider as ParticleFactory
import net.minecraft.client.particle.SuspendedTownParticle as SuspendParticle

fun initParticleClientModule() {
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MISSION.particleType, SuspendParticle::HappyVillagerProvider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.COLLECTING_MAGIC.particleType, EnchantGlyphParticle::Provider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DESCENDING_MAGIC.particleType, EndRodParticle::Provider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MIRAGE_FLOUR.particleType, SuspendParticle::HappyVillagerProvider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.ATTRACTING_MAGIC.particleType, AttractingParticle::Factory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.AURA.particleType) { spriteProvider ->
        val factory = EndRodParticle.Provider(spriteProvider)
        ParticleFactory { parameters, world, x, y, z, velocityX, velocityY, velocityZ ->
            factory.createParticle(parameters, world, x, y, z, velocityX, velocityY, velocityZ)?.also { particle ->
                particle.lifetime = 20 + world.random.nextInt(12)
            }
        }
    }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.CHAOS_STONE.particleType, createRollingFallingParticleFactory(0.0F))
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType, createRollingFallingParticleFactory(1.0F))
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DRIPPING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleFactory { _, world, x, y, z, _, _, _ -> HaimeviskaSapParticle.Dripping(world, x, y, z, spriteProvider, ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleFactory { _, world, x, y, z, _, _, _ -> HaimeviskaSapParticle.Falling(world, x, y, z, spriteProvider, ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleFactory { _, world, x, y, z, _, _, _ -> HaimeviskaSapParticle.Landing(world, x, y, z, spriteProvider) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MAGIC_SQUARE.particleType, createMagicSquareParticleFactory())

    initMagicSquareParticle()
}
