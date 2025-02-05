package miragefairy2024.mod.passiveskill

import miragefairy2024.util.empty
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.text.Text

object StatusEffectPassiveSkillEffect : PassiveSkillEffectCard<StatusEffectPassiveSkillEffect.Value>("status_effect") {
    class Value(val map: Map<StatusEffect, Entry>)
    class Entry(val level: Int, val additionalSeconds: Int)

    override fun getText(value: Value): Text {
        return value.map.map { (statusEffect, entry) ->
            text { statusEffect.name + if (entry.level >= 2) " "() + entry.level.toRomanText() else empty() }
        }.join(text { ","() })
    }

    override val unit = Value(mapOf())
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (statusEffect, bEntry) ->
            val aEntry = map[statusEffect]
            if (aEntry == null || aEntry.level < bEntry.level || aEntry.additionalSeconds < bEntry.additionalSeconds) {
                map[statusEffect] = bEntry
            }
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) {
        newValue.map.forEach { (statusEffect, entry) ->
            context.player.addStatusEffect(StatusEffectInstance(statusEffect, 20 * (1 + 1 + entry.additionalSeconds), entry.level - 1, true, false, true))
        }
    }
}
