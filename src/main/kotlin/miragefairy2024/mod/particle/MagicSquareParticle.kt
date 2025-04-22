package miragefairy2024.mod.particle

import com.mojang.brigadier.StringReader
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Channel
import miragefairy2024.util.string
import net.minecraft.core.particles.ParticleType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.core.particles.ParticleOptions as ParticleEffect
import net.minecraft.world.phys.Vec3 as Vec3d

class MagicSquareParticleType(alwaysSpawn: Boolean) : ParticleType<MagicSquareParticleEffect>(alwaysSpawn, MagicSquareParticleEffect.FACTORY) {
    companion object {
        val CODEC: MapCodec<MagicSquareParticleEffect> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("layer").forGetter(MagicSquareParticleEffect::layer),
                Vec3d.CODEC.fieldOf("targetPosition").forGetter(MagicSquareParticleEffect::targetPosition),
                Codec.FLOAT.fieldOf("delay").forGetter(MagicSquareParticleEffect::delay),
            ).apply(instance, ::MagicSquareParticleEffect)
        }
    }

    override fun codec() = CODEC
}

class MagicSquareParticleEffect(val layer: Int, val targetPosition: Vec3d, val delay: Float) : ParticleEffect {
    companion object {
        val FACTORY = object : ParticleEffect.Deserializer<MagicSquareParticleEffect> {
            override fun fromNetwork(type: ParticleType<MagicSquareParticleEffect>, buf: FriendlyByteBuf): MagicSquareParticleEffect {
                val layer = buf.readInt()
                val targetPositionX = buf.readDouble()
                val targetPositionY = buf.readDouble()
                val targetPositionZ = buf.readDouble()
                val delay = buf.readFloat()
                return MagicSquareParticleEffect(layer, Vec3d(targetPositionX, targetPositionY, targetPositionZ), delay)
            }

            override fun fromCommand(type: ParticleType<MagicSquareParticleEffect>, reader: StringReader): MagicSquareParticleEffect {
                val layer = reader.readInt()
                val targetPositionX = reader.readDouble()
                val targetPositionY = reader.readDouble()
                val targetPositionZ = reader.readDouble()
                val delay = reader.readFloat()
                return MagicSquareParticleEffect(layer, Vec3d(targetPositionX, targetPositionY, targetPositionZ), delay)
            }
        }
    }

    override fun getType() = ParticleTypeCard.MAGIC_SQUARE.particleType

    override fun writeToNetwork(buf: FriendlyByteBuf) {
        buf.writeInt(layer)
        buf.writeDouble(targetPosition.x)
        buf.writeDouble(targetPosition.y)
        buf.writeDouble(targetPosition.z)
        buf.writeFloat(delay)
    }

    override fun writeToString() = ParticleTypeCard.MAGIC_SQUARE.identifier.string
}

object MagicSquareParticleChannel : Channel<MagicSquareParticlePacket>(MirageFairy2024.identifier("magic_square_particle")) {
    override fun writeToBuf(buf: FriendlyByteBuf, packet: MagicSquareParticlePacket) {
        buf.writeDouble(packet.position.x)
        buf.writeDouble(packet.position.y)
        buf.writeDouble(packet.position.z)
        buf.writeDouble(packet.targetPosition.x)
        buf.writeDouble(packet.targetPosition.y)
        buf.writeDouble(packet.targetPosition.z)
    }

    override fun readFromBuf(buf: FriendlyByteBuf): MagicSquareParticlePacket {
        val positionX = buf.readDouble()
        val positionY = buf.readDouble()
        val positionZ = buf.readDouble()
        val targetPositionX = buf.readDouble()
        val targetPositionY = buf.readDouble()
        val targetPositionZ = buf.readDouble()
        return MagicSquareParticlePacket(Vec3d(positionX, positionY, positionZ), Vec3d(targetPositionX, targetPositionY, targetPositionZ))
    }
}

class MagicSquareParticlePacket(val position: Vec3d, val targetPosition: Vec3d)
