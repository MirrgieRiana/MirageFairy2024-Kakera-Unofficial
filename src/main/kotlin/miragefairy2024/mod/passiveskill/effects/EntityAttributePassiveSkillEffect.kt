package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.text
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.text.Text
import java.util.UUID

object EntityAttributePassiveSkillEffect : AbstractPassiveSkillEffect<EntityAttributePassiveSkillEffect.Value>("entity_attribute") {
    val FORMATTERS = mutableMapOf<EntityAttribute, (Double) -> String>()

    private val defaultFormatter: (Double) -> String = { it formatAs "%+.2f" }
    private val uuid: UUID = UUID.fromString("AEC6063C-2320-4FAC-820D-0562438ECAAC")

    class Value(val map: Map<EntityAttribute, Double>)

    override fun getText(value: Value): Text {
        return value.map.map { (attribute, value) ->
            text { translate(attribute.translationKey) + " ${FORMATTERS.getOrElse(attribute) { defaultFormatter }(value)}"() }
        }.join(text { ","() })
    }

    override val unit = Value(mapOf())
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val map = a.map.toMutableMap()
        b.map.forEach { (attribute, value) ->
            map[attribute] = map.getOrElse(attribute) { 0.0 } + value
        }
        return Value(map)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) {

        // 削除するべきものを削除
        oldValue.map.forEach { (attribute, _) ->
            val customInstance = context.player.attributes.getCustomInstance(attribute) ?: return@forEach
            if (attribute !in newValue.map) {
                customInstance.removeModifier(uuid)
            }
        }

        // 追加および変更
        newValue.map.forEach { (attribute, value) ->
            val customInstance = context.player.attributes.getCustomInstance(attribute) ?: return@forEach
            val oldModifier = customInstance.getModifier(uuid)
            if (oldModifier == null) {
                val modifier = EntityAttributeModifier(uuid, "Fairy Bonus", value, EntityAttributeModifier.Operation.ADDITION)
                customInstance.addTemporaryModifier(modifier)
            } else if (oldModifier.value != value) {
                customInstance.removeModifier(uuid)
                val modifier = EntityAttributeModifier(uuid, "Fairy Bonus", value, EntityAttributeModifier.Operation.ADDITION)
                customInstance.addTemporaryModifier(modifier)
            }
        }

    }
}
