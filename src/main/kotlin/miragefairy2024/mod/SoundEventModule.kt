package miragefairy2024.mod

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundEvent

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
    ;

    val identifier = MirageFairy2024.identifier(path)
    val sounds = soundPaths.map { MirageFairy2024.identifier(it) }
    val translation = Translation({ identifier.toTranslationKey("subtitles") }, en, ja)
    val soundEvent: SoundEvent = SoundEvent.of(identifier)
}

context(ModContext)
fun initSoundEventModule() {
    SoundEventCard.entries.forEach { card ->
        card.soundEvent.register(Registries.SOUND_EVENT, card.identifier)
        DataGenerationEvents.onGenerateSound { it(card.path, card.translation.keyGetter(), card.sounds) }
        card.translation.enJa()
    }
}
