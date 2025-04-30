package miragefairy2024.util

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.world.phys.Vec3 as Vec3d

abstract class Channel<P>(packetId: ResourceLocation) {
    val streamCodec = object : StreamCodec<RegistryFriendlyByteBuf, Payload<P>> {
        override fun encode(`object`: RegistryFriendlyByteBuf, object2: Payload<P>) {
            writeToBuf(`object`, object2.data)
        }

        override fun decode(`object`: RegistryFriendlyByteBuf): Payload<P> {
            return Payload(this@Channel, readFromBuf(`object`))
        }
    }
    val type = CustomPacketPayload.Type<Payload<P>>(packetId)

    abstract fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: P)
    abstract fun readFromBuf(buf: RegistryFriendlyByteBuf): P

    class Payload<P>(val channel: Channel<P>, val data: P) : CustomPacketPayload {
        override fun type() = channel.type
    }
}

fun <P> Channel<P>.sendToClient(player: ServerPlayerEntity, packet: P) {
    ServerPlayNetworking.send(player, Channel.Payload(this, packet))
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
    PayloadTypeRegistry.playC2S().register(this.type, this.streamCodec)
    ServerPlayNetworking.registerGlobalReceiver(this.type) { payload, context ->
        // ここはネットワークスレッドなのでここで player にアクセスすることはできない
        context.server().execute {
            handler(context.player(), payload.data)
        }
    }
}
