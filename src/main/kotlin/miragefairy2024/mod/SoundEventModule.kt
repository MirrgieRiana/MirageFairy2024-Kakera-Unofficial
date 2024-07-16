package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.ModEvents
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

enum class SoundEventCard(val path: String, en: String, ja: String, soundPaths: List<String>) {
    MAGIC1("magic1", "Magic fired", "魔法が発射される", listOf("magic1")),
    MAGIC2("magic2", "Magic fired", "魔法が発射される", listOf("magic2")),
    MAGIC3("magic3", "Magic fired", "魔法が発射される", listOf("magic3")),
    MAGIC_HIT("magic_hit", "Magic hits", "魔法が当たる", listOf("magic_hit")),
    CANCEL("cancel", "TODO", "TODO", listOf("cancel")), // TODO
    COLLECT("collect", "Collect item", "アイテムを集める", listOf("collect")),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val sounds = soundPaths.map { Identifier(MirageFairy2024.modId, it) }
    val translation = Translation({ identifier.toTranslationKey("subtitles") }, en, ja)
    val soundEvent: SoundEvent = SoundEvent.of(identifier)
}

fun initSoundEventModule() = ModEvents.onInitialize {
    SoundEventCard.entries.forEach { card ->
        card.soundEvent.register(Registries.SOUND_EVENT, card.identifier)
        MirageFairy2024DataGenerator.soundGenerators { it(card.path, card.translation.keyGetter(), card.sounds) }
        card.translation.enJa()
    }
}
