package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.invoke
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.empty
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.text.Text

object ManaBoostPassiveSkillEffect : AbstractPassiveSkillEffect<ManaBoostPassiveSkillEffect.Value>("mana_boost") {
    class Value(val map: Map<Motif?, Double>)

    override val isPreprocessor = true
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Mana", "魔力")
    private val familyTranslation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}.family" }, "%s Family", "%s系統")
    override fun getText(value: Value): Text {
        return value.map.map { (motif, value) ->
            text { translation() + ": "() + Emoji.MANA() + (value * 100 formatAs "%+.0f%%")() + if (motif != null) " ("() + familyTranslation(motif.displayName) + ")"() else empty() }
        }.join(text { ","() })
    }

    override val unit = Value(mapOf())
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (motif, value) ->
            map[motif] = map.getOrElse(motif) { 0.0 } + value
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) = Unit
    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()
        familyTranslation.enJa()
    }
}
