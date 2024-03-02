package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import net.minecraft.util.Identifier

fun initPassiveSkillEffects() {
    PassiveSkillEffectCard.entries.forEach { card ->
        card.register(passiveSkillEffectRegistry, card.identifier)
        card.translations.forEach {
            it.enJa()
        }
    }
}

abstract class PassiveSkillEffectCard<T>(path: String) : PassiveSkillEffect<T> {
    companion object {
        val entries = mutableListOf<PassiveSkillEffectCard<*>>()
        private operator fun <T> PassiveSkillEffectCard<T>.unaryPlus() = this.also { entries += it }
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    abstract val translations: List<Translation>
}
