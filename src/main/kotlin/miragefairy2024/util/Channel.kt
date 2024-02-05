package miragefairy2024.util

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

abstract class Channel<P>(val packetId: Identifier) {
    abstract fun writeToBuf(buf: PacketByteBuf, player: PlayerEntity, packet: P)
    abstract fun readFromBuf(buf: PacketByteBuf, player: PlayerEntity): P
}

fun <P> Channel<P>.sendToClient(player: ServerPlayerEntity, packet: P) {
    val buf = PacketByteBufs.create()
    this.writeToBuf(buf, player, packet)
    ServerPlayNetworking.send(player, this.packetId, buf)
}

fun <P> Channel<P>.registerServerPacketReceiver(handler: (ServerPlayerEntity, P) -> Unit) {
    ServerPlayNetworking.registerGlobalReceiver(this.packetId) { server, player, _, buf, _ ->
        val data = this.readFromBuf(buf, player)
        server.execute {
            handler(player, data)
        }
    }
}
