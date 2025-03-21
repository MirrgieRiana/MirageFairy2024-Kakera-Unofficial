package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.DamageCallback
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.passiveskill.passiveSkillResult
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.registerDamageTypeTagGeneration
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text

object ElementPassiveSkillEffect : AbstractPassiveSkillEffect<ElementPassiveSkillEffect.Value>("element") {
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
        SPINE("spine", "Spine", "棘", { it.isIn(SPINE_DAMAGE_TYPE_TAG) }),
        ;

        val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toTranslationKey()}.elements.$path" }, enName, jaName)
        override val text = text { translation() }
        override fun test(damageSource: DamageSource) = predicate(damageSource)
    }

    private val SPINE_DAMAGE_TYPE_TAG = TagKey.of(RegistryKeys.DAMAGE_TYPE, MirageFairy2024.identifier("spine"))

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
        super.init()
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
                attacker.passiveSkillResult[ElementPassiveSkillEffect].attackMap.forEach { (element, value) ->
                    if (element.test(source)) {
                        attackBonus += value
                    }
                }
                damage *= (1.0 + attackBonus).toFloat()
            }

            if (entity is PlayerEntity) {
                var defenceBonus = 0.0
                entity.passiveSkillResult[ElementPassiveSkillEffect].defenceMap.forEach { (element, value) ->
                    if (element.test(source)) {
                        defenceBonus += value
                    }
                }
                damage /= (1.0 + defenceBonus).toFloat()
            }

            damage
        }

        DamageTypes.CACTUS.value.registerDamageTypeTagGeneration { SPINE_DAMAGE_TYPE_TAG }
        DamageTypes.SWEET_BERRY_BUSH.value.registerDamageTypeTagGeneration { SPINE_DAMAGE_TYPE_TAG }
        DamageTypes.STING.value.registerDamageTypeTagGeneration { SPINE_DAMAGE_TYPE_TAG }
    }
}
