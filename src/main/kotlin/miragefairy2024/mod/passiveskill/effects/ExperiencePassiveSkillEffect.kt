package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs

object ExperiencePassiveSkillEffect : AbstractDoublePassiveSkillEffect("experience") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}" }, "Gain XP: %s/s", "経験値獲得: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        val actualAmount = context.world.random.randomInt(newValue)
        if (actualAmount > 0) {
            context.player.addExperience(actualAmount)
        }
    }

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
    }
}
