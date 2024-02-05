package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Channel
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

object GainFairyDreamChannel : Channel<Motif>(Identifier(MirageFairy2024.modId, "gain_fairy_dream")) {
    override fun writeToBuf(buf: PacketByteBuf, player: PlayerEntity, packet: Motif) {
        buf.writeString(packet.getIdentifier()!!.string)
    }

    override fun readFromBuf(buf: PacketByteBuf, player: PlayerEntity): Motif {
        val motifId = buf.readString()
        return motifRegistry.get(motifId.toIdentifier())!!
    }
}
