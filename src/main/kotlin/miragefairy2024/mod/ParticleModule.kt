package miragefairy2024.mod

import com.mojang.brigadier.StringReader
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Channel
import miragefairy2024.util.register
import miragefairy2024.util.string
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class ParticleTypeCard<P : ParticleType<T>, T : ParticleEffect>(
    path: String,
    textureNames: List<String>,
    creator: () -> P,
) {
    companion object {
        val entries = mutableListOf<ParticleTypeCard<*, *>>()
        private operator fun <P : ParticleType<T>, T : ParticleEffect> ParticleTypeCard<P, T>.not() = this.also { entries.add(this) }

        val MISSION = !ParticleTypeCard("mission", listOf("mission")) { FabricParticleTypes.simple(true) }
        val COLLECTING_MAGIC = !ParticleTypeCard("collecting_magic", listOf("magic")) { FabricParticleTypes.simple(false) }
        val DESCENDING_MAGIC = !ParticleTypeCard("descending_magic", listOf("magic")) { FabricParticleTypes.simple(false) }
        val MIRAGE_FLOUR = !ParticleTypeCard("mirage_flour", listOf("mirage_flour")) { FabricParticleTypes.simple(false) }
        val ATTRACTING_MAGIC = !ParticleTypeCard("attracting_magic", listOf("mission")) { FabricParticleTypes.simple(false) }
        val AURA = !ParticleTypeCard("aura", listOf("mission")) { FabricParticleTypes.simple(false) }
        val CHAOS_STONE = !ParticleTypeCard("chaos_stone", listOf("chaos_stone")) { FabricParticleTypes.simple(false) }
        val HAIMEVISKA_BLOSSOM = !ParticleTypeCard("haimeviska_blossom", listOf("haimeviska_blossom")) { FabricParticleTypes.simple(false) }
        val DRIPPING_HAIMEVISKA_SAP = !ParticleTypeCard("dripping_haimeviska_sap", listOf("minecraft:drip_hang")) { FabricParticleTypes.simple(false) }
        val FALLING_HAIMEVISKA_SAP = !ParticleTypeCard("falling_haimeviska_sap", listOf("minecraft:drip_fall")) { FabricParticleTypes.simple(false) }
        val LANDING_HAIMEVISKA_SAP = !ParticleTypeCard("landing_haimeviska_sap", listOf("minecraft:drip_land")) { FabricParticleTypes.simple(false) }
        val MAGIC_SQUARE = !ParticleTypeCard("magic_square", (1..7).map { "magic_square_$it" }) { MagicSquareParticleType(true) }
    }

    val identifier = MirageFairy2024.identifier(path)
    val textures = textureNames.map { if (":" in it) Identifier(it) else MirageFairy2024.identifier(it) }
    val particleType: P = creator()
}

context(ModContext)
fun initParticleModule() {
    ParticleTypeCard.entries.forEach { card ->
        card.particleType.register(Registries.PARTICLE_TYPE, card.identifier)
        DataGenerationEvents.onGenerateParticles {
            val data = jsonObject(
                "textures" to card.textures.map { it.string.jsonElement }.jsonArray,
            )
            it(card.identifier, data)
        }
    }
}

class MagicSquareParticleType(alwaysSpawn: Boolean) : ParticleType<MagicSquareParticleEffect>(alwaysSpawn, MagicSquareParticleEffect.FACTORY) {
    companion object {
        val CODEC: Codec<MagicSquareParticleEffect> = RecordCodecBuilder.create { instance ->
            instance.group(
                Vec3d.CODEC.fieldOf("targetPosition").forGetter(MagicSquareParticleEffect::targetPosition),
            ).apply(instance, ::MagicSquareParticleEffect)
        }
    }

    override fun getCodec() = CODEC
}

class MagicSquareParticleEffect(val targetPosition: Vec3d) : ParticleEffect {
    companion object {
        val FACTORY = object : ParticleEffect.Factory<MagicSquareParticleEffect> {
            override fun read(type: ParticleType<MagicSquareParticleEffect>, buf: PacketByteBuf): MagicSquareParticleEffect {
                return MagicSquareParticleEffect(Vec3d.ZERO) // TODO
            }

            override fun read(type: ParticleType<MagicSquareParticleEffect>, reader: StringReader): MagicSquareParticleEffect {
                val targetPositionX = reader.readDouble()
                val targetPositionY = reader.readDouble()
                val targetPositionZ = reader.readDouble()
                return MagicSquareParticleEffect(Vec3d(targetPositionX, targetPositionY, targetPositionZ))
            }
        }
    }

    override fun getType() = ParticleTypeCard.MAGIC_SQUARE.particleType

    override fun write(buf: PacketByteBuf) {
        buf.writeDouble(targetPosition.x)
        buf.writeDouble(targetPosition.y)
        buf.writeDouble(targetPosition.z)
    }

    override fun asString() = ParticleTypeCard.MAGIC_SQUARE.identifier.string
}

object MagicSquareParticleChannel : Channel<MagicSquareParticlePacket>(MirageFairy2024.identifier("magic_square_particle")) {
    override fun writeToBuf(buf: PacketByteBuf, packet: MagicSquareParticlePacket) {
        buf.writeDouble(packet.position.x)
        buf.writeDouble(packet.position.y)
        buf.writeDouble(packet.position.z)
        buf.writeDouble(packet.targetPosition.x)
        buf.writeDouble(packet.targetPosition.y)
        buf.writeDouble(packet.targetPosition.z)
    }

    override fun readFromBuf(buf: PacketByteBuf): MagicSquareParticlePacket {
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
