package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.DoublePassiveSkillEffectCard
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs

object HungerPassiveSkillEffect : DoublePassiveSkillEffectCard("hunger") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Hunger: %s/s", "空腹: %s/秒")
    override fun getText(value: Double) = text { translation(-value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        context.player.addExhaustion(newValue.toFloat() * 4F)
    }

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
    }
}
