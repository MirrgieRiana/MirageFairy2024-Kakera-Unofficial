package miragefairy2024.client.util

import miragefairy2024.util.Channel
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient

fun <P> Channel<P>.sendToServer(packet: P) {
    val buf = PacketByteBufs.create()
    this.writeToBuf(buf, MinecraftClient.getInstance().player!!, packet)
    ClientPlayNetworking.send(packetId, buf)
}

fun <P> Channel<P>.registerClientPacketReceiver(handler: (P) -> Unit) {
    ClientPlayNetworking.registerGlobalReceiver(this.packetId) { client, _, buf, _ ->
        val data = this.readFromBuf(buf, client.player!!)
        client.execute {
            handler(data)
        }
    }
}
