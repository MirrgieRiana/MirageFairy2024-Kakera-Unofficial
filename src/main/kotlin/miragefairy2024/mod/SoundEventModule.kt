package miragefairy2024.mod

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Channel
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos

enum class SoundEventCard(val path: String, en: String, ja: String, soundPaths: List<String>) {
    MAGIC1("magic1", "Magic fired", "魔法が発射される", listOf("magic1")),
    MAGIC2("magic2", "Magic fired", "魔法が発射される", listOf("magic2")),
    MAGIC3("magic3", "Magic fired", "魔法が発射される", listOf("magic3")),
    MAGIC_HIT("magic_hit", "Magic hits", "魔法が当たる", listOf("magic_hit")),
    CANCEL("cancel", "TODO", "TODO", listOf("cancel")), // TODO
    COLLECT("collect", "Collect item", "アイテムを集める", listOf("collect")),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val sounds = soundPaths.map { MirageFairy2024.identifier(it) }
    val translation = Translation({ identifier.toTranslationKey("subtitles") }, en, ja)
    val soundEvent: SoundEvent = SoundEvent.of(identifier)
}

object SoundEventChannel : Channel<SoundEventPacket>(MirageFairy2024.identifier("sound")) {
    override fun writeToBuf(buf: PacketByteBuf, packet: SoundEventPacket) {
        buf.writeIdentifier(Registries.SOUND_EVENT.getId(packet.soundEvent))
        buf.writeBlockPos(packet.pos)
        buf.writeString(packet.category.name)
        buf.writeFloat(packet.volume)
        buf.writeFloat(packet.pitch)
        buf.writeBoolean(packet.useDistance)
    }

    override fun readFromBuf(buf: PacketByteBuf): SoundEventPacket {
        val soundEvent = Registries.SOUND_EVENT.get(buf.readIdentifier())!!
        val pos = buf.readBlockPos()
        val category = buf.readString().let { name -> SoundCategory.entries.first { it.name == name } }
        val volume = buf.readFloat()
        val pitch = buf.readFloat()
        val useDistance = buf.readBoolean()
        return SoundEventPacket(soundEvent, pos, category, volume, pitch, useDistance)
    }
}

class SoundEventPacket(
    val soundEvent: SoundEvent,
    val pos: BlockPos,
    val category: SoundCategory,
    val volume: Float,
    val pitch: Float,
    val useDistance: Boolean,
)

context(ModContext)
fun initSoundEventModule() {
    SoundEventCard.entries.forEach { card ->
        card.soundEvent.register(Registries.SOUND_EVENT, card.identifier)
        DataGenerationEvents.onGenerateSound { it(card.path, card.translation.keyGetter(), card.sounds) }
        card.translation.enJa()
    }
}
