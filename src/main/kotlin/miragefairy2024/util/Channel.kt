package miragefairy2024.util

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.FriendlyByteBuf as PacketByteBuf
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.world.phys.Vec3 as Vec3d

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
    world.players().forEach { player ->
        if (player.level().dimension() == world.dimension()) {
            if (pos.distanceToSqr(player.position()) <= distance * distance) {
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
