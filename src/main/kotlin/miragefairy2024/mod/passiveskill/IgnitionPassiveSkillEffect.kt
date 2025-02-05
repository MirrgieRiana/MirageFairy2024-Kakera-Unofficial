package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.empty
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atLeast

object IgnitionPassiveSkillEffect : PassiveSkillEffectCard<Boolean>("ignition") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Ignition", "発火")
    override fun getText(value: Boolean) = text { if (value) translation() else empty() }
    override val unit = false
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun combine(a: Boolean, b: Boolean) = a || b
    override fun update(context: PassiveSkillContext, oldValue: Boolean, newValue: Boolean) {
        if (newValue) {
            if (context.player.isWet || context.player.inPowderSnow || context.player.wasInPowderSnow) return
            context.player.fireTicks = 30 atLeast context.player.fireTicks
        }
    }

    context(ModContext)
    override fun init() {
        translation.enJa()
    }
}
