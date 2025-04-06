package miragefairy2024.client.mod.particle

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.minecraft.client.particle.ParticleFactory
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.particle.SpriteProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import kotlin.math.roundToInt

fun createMagicSquareParticleFactory() = { spriteProvider: SpriteProvider ->
    ParticleFactory<DefaultParticleType> { _, world, x, y, z, velocityX, _, _ ->
        MagicSquareParticle(world, x, y, z, velocityX.roundToInt(), spriteProvider)
    }
}

class MagicSquareParticle(world: ClientWorld, x: Double, y: Double, z: Double, index: Int, spriteProvider: SpriteProvider) : SpriteBillboardParticle(world, x, y, z) {

    init {
        setSprite((spriteProvider as FabricSpriteProvider).sprites[index])
        gravityStrength = 0.0F
        maxAge = 40
        scale = 2.0F
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_LIT

    override fun tick() {
        prevPosX = x
        prevPosY = y
        prevPosZ = z
        age++
        if (age >= maxAge) markDead()
    }
}
