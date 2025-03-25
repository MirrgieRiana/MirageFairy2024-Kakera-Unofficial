package miragefairy2024.client.mod

import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.mod.SoundEventChannel
import net.minecraft.client.MinecraftClient

fun initSoundEventClientModule() {
    SoundEventChannel.registerClientPacketReceiver { packet ->
        val client = MinecraftClient.getInstance() ?: return@registerClientPacketReceiver
        val world = client.world ?: return@registerClientPacketReceiver
        world.playSoundAtBlockCenter(
            packet.pos,
            packet.soundEvent,
            packet.category,
            packet.volume,
            packet.pitch,
            packet.useDistance,
        )
    }
}
