package miragefairy2024.client.util

import miragefairy2024.util.Channel
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

fun <P> Channel<P>.sendToServer(packet: P) {
    ClientPlayNetworking.send(Channel.Payload(this, packet))
}

fun <P> Channel<P>.registerClientPacketReceiver(handler: (P) -> Unit) {
    PayloadTypeRegistry.playS2C().register(this.type, this.streamCodec)
    ClientPlayNetworking.registerGlobalReceiver(this.type) { payload, context ->
        // ここはネットワークスレッドなのでここで player にアクセスすることはできない
        context.client().execute {
            handler(payload.data)
        }
    }
}
