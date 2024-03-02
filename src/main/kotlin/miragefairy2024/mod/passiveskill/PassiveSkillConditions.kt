package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import net.minecraft.util.Identifier

fun initPassiveSkillConditions() {
    PassiveSkillConditionCard.entries.forEach { card ->
        card.translations.forEach {
            it.enJa()
        }
    }
}

abstract class PassiveSkillConditionCard(path: String) : PassiveSkillCondition {
    companion object {
        val entries = mutableListOf<PassiveSkillConditionCard>()
        private operator fun PassiveSkillConditionCard.unaryPlus() = this.also { entries += it }
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    abstract val translations: List<Translation>
}
