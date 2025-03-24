package miragefairy2024.util

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

abstract class Channel<P>(val packetId: Identifier) {
    abstract fun writeToBuf(buf: PacketByteBuf, packet: P)
    abstract fun readFromBuf(buf: PacketByteBuf): P
}

fun <P> Channel<P>.sendToClient(player: ServerPlayerEntity, packet: P) {
    val buf = PacketByteBufs.create()
    this.writeToBuf(buf, packet)
    ServerPlayNetworking.send(player, this.packetId, buf)
}

fun <P> Channel<P>.sendToAround(world: ServerWorld, pos: Vec3d, distance: Double, packet: P) {
    world.players.forEach { player ->
        if (player.world.registryKey == world.registryKey) {
            val x = pos.x - player.x
            val y = pos.y - player.y
            val z = pos.z - player.z
            if (x * x + y * y + z * z < distance * distance) {
                this.sendToClient(player, packet)
            }
        }
    }
}

fun <P> Channel<P>.registerServerPacketReceiver(handler: (ServerPlayerEntity, P) -> Unit) {
    ServerPlayNetworking.registerGlobalReceiver(this.packetId) { server, player, _, buf, _ ->
        val data = this.readFromBuf(buf) // ここはネットワークスレッドなのでここで player にアクセスすることはできない
        server.execute {
            handler(player, data)
        }
    }
}
