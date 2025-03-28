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
    ENTITY_CHAOS_CUBE_AMBIENT("entity.chaos_cube.ambient", "Chaos Cube roars", "混沌のキューブがうなる", listOf("entity_chaos_cube_ambient_1", "entity_chaos_cube_ambient_2")),
    ENTITY_CHAOS_CUBE_HURT("entity.chaos_cube.hurt", "Chaos Cube hurts", "混沌のキューブがダメージを受ける", listOf("entity_chaos_cube_hurt_1")),
    ENTITY_CHAOS_CUBE_DEATH("entity.chaos_cube.death", "Chaos Cube dies", "混沌のキューブが死ぬ", listOf("entity_chaos_cube_death_1")),
    ENTITY_CHAOS_CUBE_ATTACK("entity.chaos_cube.attack", "Chaos Cube chants", "混沌のキューブが詠唱する", listOf("entity_chaos_cube_attack_1")),
    ENTITY_ETHEROBALLISTIC_ZERO_GRAVITY_BOLT_HIT("entity.etheroballistic_zero_gravity_bolt.hit", "Etheroballistic Zero Gravity Bolt hits", "エテロバリスティック無重力弾が当たる", listOf("entity_etheroballistic_zero_gravity_bolt_hit_1")),
    ENTITY_ETHEROBALLISTIC_ZERO_GRAVITY_BOLT_SHOOT("entity.etheroballistic_zero_gravity_bolt.shoot", "Etheroballistic Zero Gravity Bolt fired", "エテロバリスティック無重力弾が発射される", listOf("entity_etheroballistic_zero_gravity_bolt_shoot_1")),
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
