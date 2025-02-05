package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.BooleanPassiveSkillEffectCard
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.empty
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atLeast

object IgnitionPassiveSkillEffect : BooleanPassiveSkillEffectCard("ignition") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Ignition", "発火")
    override fun getText(value: Boolean) = text { if (value) translation() else empty() }
    override fun update(context: PassiveSkillContext, oldValue: Boolean, newValue: Boolean) {
        if (!newValue) return
        if (context.player.isWet || context.player.inPowderSnow || context.player.wasInPowderSnow) return
        context.player.fireTicks = 30 atLeast context.player.fireTicks
    }

    context(ModContext)
    override fun init() {
        translation.enJa()
    }
}
