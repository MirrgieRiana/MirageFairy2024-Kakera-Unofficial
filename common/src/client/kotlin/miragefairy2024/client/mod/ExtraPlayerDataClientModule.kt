package miragefairy2024.client.mod

import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.ExtraPlayerDataSynchronizationChannel
import miragefairy2024.mod.ExtraPlayerDataSynchronizationPacket
import miragefairy2024.mod.extraPlayerDataContainer
import net.minecraft.client.Minecraft as MinecraftClient

fun initExtraPlayerDataClientModule() {
    ExtraPlayerDataSynchronizationChannel.registerClientPacketReceiver { packet ->
        fun <T : Any> f(packet: ExtraPlayerDataSynchronizationPacket<T>) {
            MinecraftClient.getInstance().player!!.extraPlayerDataContainer[packet.category] = packet.value
        }
        f(packet)
    }
}
