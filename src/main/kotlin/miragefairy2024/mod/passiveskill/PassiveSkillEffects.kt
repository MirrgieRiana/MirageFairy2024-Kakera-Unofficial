package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mixin.api.DamageCallback
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.invoke
import miragefairy2024.util.Translation
import miragefairy2024.util.collectItem
import miragefairy2024.util.empty
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.repair
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.text.Text
import java.util.UUID

context(ModContext)
fun initPassiveSkillEffects() {
    PassiveSkillEffectCard.entries.forEach { card ->
        card.register(passiveSkillEffectRegistry, card.identifier)
        card.init()
    }

    ModEvents.onInitialize {
        EntityAttributePassiveSkillEffect.formatters[EntityAttributes.GENERIC_MOVEMENT_SPEED] = { (it / 0.1) * 100 formatAs "%+.0f%%" }
    }
}

abstract class PassiveSkillEffectCard<T>(path: String) : PassiveSkillEffect<T> {
    companion object {
        val entries = mutableListOf<PassiveSkillEffectCard<*>>()
        private operator fun <T> PassiveSkillEffectCard<T>.unaryPlus() = this.also { entries += it }

        val MANA_BOOST = +ManaBoostPassiveSkillEffect
        val ENTITY_ATTRIBUTE = +EntityAttributePassiveSkillEffect
        val STATUS_EFFECT = +StatusEffectPassiveSkillEffect
        val IGNITION = +IgnitionPassiveSkillEffect
        val EXPERIENCE = +ExperiencePassiveSkillEffect
        val REGENERATION = +RegenerationPassiveSkillEffect
        val HUNGER = +HungerPassiveSkillEffect
        val MENDING = +MendingPassiveSkillEffect
        val COLLECTION = +CollectionPassiveSkillEffect
        val ELEMENT = +ElementPassiveSkillEffect
    }

    val identifier = MirageFairy2024.identifier(path)
    context(ModContext)
    open fun init() = Unit

    override val isPreprocessor = false
}

object ManaBoostPassiveSkillEffect : PassiveSkillEffectCard<ManaBoostPassiveSkillEffect.Value>("mana_boost") {
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
        translation.enJa()
        familyTranslation.enJa()
    }
}

object EntityAttributePassiveSkillEffect : PassiveSkillEffectCard<EntityAttributePassiveSkillEffect.Value>("entity_attribute") {
    val formatters = mutableMapOf<EntityAttribute, (Double) -> String>()
    private val defaultFormatter: (Double) -> String = { it formatAs "%+.2f" }
    private val uuid: UUID = UUID.fromString("AEC6063C-2320-4FAC-820D-0562438ECAAC")

    class Value(val map: Map<EntityAttribute, Double>)

    override fun getText(value: Value): Text {
        return value.map.map { (attribute, value) ->
            text { translate(attribute.translationKey) + " ${formatters.getOrElse(attribute) { defaultFormatter }(value)}"() }
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

abstract class DoublePassiveSkillEffectCard(path: String) : PassiveSkillEffectCard<Double>(path) {
    override val unit = 0.0
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
}

object ExperiencePassiveSkillEffect : DoublePassiveSkillEffectCard("experience") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Gain XP: %s/s", "経験値獲得: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue > 0.0) {
            val actualAmount = context.world.random.randomInt(newValue)
            if (actualAmount > 0) {
                context.player.addExperience(actualAmount)
            }
        }
    }

    context(ModContext)
    override fun init() {
        translation.enJa()
    }
}

object RegenerationPassiveSkillEffect : DoublePassiveSkillEffectCard("regeneration") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Regeneration: %s/s", "持続回復: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue > 0.0) {
            if (context.player.health < context.player.maxHealth) {
                context.player.heal(newValue.toFloat())
            }
        }
    }

    context(ModContext)
    override fun init() {
        translation.enJa()
    }
}

object HungerPassiveSkillEffect : DoublePassiveSkillEffectCard("hunger") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Hunger: %s/s", "空腹: %s/秒")
    override fun getText(value: Double) = text { translation(-value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        context.player.addExhaustion(newValue.toFloat() * 4F)
    }

    context(ModContext)
    override fun init() {
        translation.enJa()
    }
}

object MendingPassiveSkillEffect : DoublePassiveSkillEffectCard("mending") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Mending: %s/s", "修繕: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        if (newValue <= 0.0) return
        context.player.mainHandStack.repair(context.world.random.randomInt(newValue))
    }

    context(ModContext)
    override fun init() {
        translation.enJa()
    }
}

object CollectionPassiveSkillEffect : DoublePassiveSkillEffectCard("collection") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}" }, "Collection: %s/s", "収集: %s/秒")
    override fun getText(value: Double) = text { translation(value formatAs "%+.3f") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) {
        val world = context.world
        val player = context.player
        if (newValue <= 0.0) return
        val actualAmount = world.random.randomInt(newValue)
        if (actualAmount <= 0) return
        collectItem(world, player.eyeBlockPos, reach = 15, maxCount = actualAmount, predicate = { !it.boundingBox.intersects(player.boundingBox) }) { // 既に触れているアイテムには無反応
            it.teleport(player.x, player.y, player.z)
            it.resetPickupDelay()
            true
        }
    }

    context(ModContext)
    override fun init() {
        translation.enJa()
    }
}

object ElementPassiveSkillEffect : PassiveSkillEffectCard<ElementPassiveSkillEffect.Value>("element") {
    class Value(val attackMap: Map<Element, Double>, val defenceMap: Map<Element, Double>)

    interface Element {
        val text: Text
        fun test(damageSource: DamageSource): Boolean
    }

    enum class Elements(path: String, enName: String, jaName: String, private val predicate: (DamageSource) -> Boolean) : Element {
        OVERALL("overall", "Overall", "全体", { true }),
        MELEE("melee", "Melee", "近接", { it.isOf(DamageTypes.PLAYER_ATTACK) || it.isOf(DamageTypes.MOB_ATTACK) || it.isOf(DamageTypes.MOB_ATTACK_NO_AGGRO) }),
        SHOOTING("shooting", "Shooting", "射撃", { it.isIn(DamageTypeTags.IS_PROJECTILE) && !it.isIn(DamageTypeTags.BYPASSES_ARMOR) }),
        MAGIC("magic", "Magic", "魔法", { it.isIn(DamageTypeTags.BYPASSES_ARMOR) }),
        FIRE("fire", "Fire", "火属性", { it.isIn(DamageTypeTags.IS_FIRE) }),
        FALL("fall", "Fall", "落下", { it.isIn(DamageTypeTags.IS_FALL) }),
        ;

        val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}.elements.$path" }, enName, jaName)
        override val text = translation()
        override fun test(damageSource: DamageSource) = predicate(damageSource)
    }

    private val attackTranslation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}.attack" }, "%s Attack", "%s攻撃力")
    private val defenceTranslation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}.defence" }, "%s Defence", "%s防御力")
    override fun getText(value: Value): Text {
        return listOf(
            value.attackMap.map { (element, value) ->
                text { attackTranslation(element.text) + ": ${value * 100 formatAs "%+.0f%%"}"() }
            },
            value.defenceMap.map { (element, value) ->
                text { defenceTranslation(element.text) + ": ${value * 100 formatAs "%+.0f%%"}"() }
            },
        ).flatten().join(text { ","() })
    }

    override val unit = Value(mapOf(), mapOf())
    override fun castOrThrow(value: Any?) = value as Value
    override fun combine(a: Value, b: Value): Value {
        val attackMap = a.attackMap.toMutableMap()
        val defenceMap = a.defenceMap.toMutableMap()
        b.attackMap.forEach { (element, bValue) ->
            val aValue = attackMap[element] ?: 0.0
            attackMap[element] = aValue + bValue
        }
        b.defenceMap.forEach { (element, bValue) ->
            val aValue = defenceMap[element] ?: 1.0
            defenceMap[element] = aValue + bValue
        }
        return Value(attackMap, defenceMap)
    }

    override fun update(context: PassiveSkillContext, oldValue: Value, newValue: Value) = Unit
    context(ModContext)
    override fun init() {
        attackTranslation.enJa()
        defenceTranslation.enJa()
        Elements.entries.forEach {
            it.translation.enJa()
        }
        DamageCallback.EVENT.register { entity, source, amount ->
            var damage = amount

            val attacker = source.attacker
            if (attacker is PlayerEntity) {
                var attackBonus = 0.0
                attacker.passiveSkillResult[ELEMENT].attackMap.forEach { (element, value) ->
                    if (element.test(source)) {
                        attackBonus += value
                    }
                }
                damage *= (1.0 + attackBonus).toFloat()
            }

            if (entity is PlayerEntity) {
                var defenceBonus = 0.0
                entity.passiveSkillResult[ELEMENT].defenceMap.forEach { (element, value) ->
                    if (element.test(source)) {
                        defenceBonus += value
                    }
                }
                damage /= (1.0 + defenceBonus).toFloat()
            }

            damage
        }
    }
}
