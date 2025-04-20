package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Channel
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import net.minecraft.network.FriendlyByteBuf

object GainFairyDreamChannel : Channel<Motif>(MirageFairy2024.identifier("gain_fairy_dream")) {
    override fun writeToBuf(buf: FriendlyByteBuf, packet: Motif) {
        buf.writeUtf(packet.getIdentifier()!!.string)
    }

    override fun readFromBuf(buf: FriendlyByteBuf): Motif {
        val motifId = buf.readUtf()
        return motifRegistry.get(motifId.toIdentifier())!!
    }
}
