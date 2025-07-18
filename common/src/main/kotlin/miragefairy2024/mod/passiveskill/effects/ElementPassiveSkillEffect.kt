package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixins.api.DamageCallback
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.passiveskill.passiveSkillResult
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.registerDamageTypeTagGeneration
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.tags.DamageTypeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.player.Player as PlayerEntity

object ElementPassiveSkillEffect : AbstractPassiveSkillEffect<ElementPassiveSkillEffect.Value>("element") {
    class Value(val attackMap: Map<Element, Double>, val defenceMap: Map<Element, Double>)

    interface Element {
        val text: Component
        fun test(damageSource: DamageSource): Boolean
    }

    enum class Elements(path: String, enName: String, jaName: String, private val predicate: (DamageSource) -> Boolean) : Element {
        OVERALL("overall", "Overall", "全体", { true }),
        MELEE("melee", "Melee", "近接", { it.`is`(DamageTypes.PLAYER_ATTACK) || it.`is`(DamageTypes.MOB_ATTACK) || it.`is`(DamageTypes.MOB_ATTACK_NO_AGGRO) }),
        SHOOTING("shooting", "Shooting", "射撃", { it.`is`(DamageTypeTags.IS_PROJECTILE) && !it.`is`(DamageTypeTags.BYPASSES_ARMOR) }),
        MAGIC("magic", "Magic", "魔法", { it.`is`(DamageTypeTags.BYPASSES_ARMOR) }),
        FIRE("fire", "Fire", "火属性", { it.`is`(DamageTypeTags.IS_FIRE) }),
        FALL("fall", "Fall", "落下", { it.`is`(DamageTypeTags.IS_FALL) }),
        SPINE("spine", "Spine", "棘", { it.`is`(SPINE_DAMAGE_TYPE_TAG) }),
        ;

        val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}.elements.$path" }, enName, jaName)
        override val text = text { translation() }
        override fun test(damageSource: DamageSource) = predicate(damageSource)
    }

    private val SPINE_DAMAGE_TYPE_TAG = TagKey.create(Registries.DAMAGE_TYPE, MirageFairy2024.identifier("spine"))

    private val attackTranslation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}.attack" }, "%s Attack", "%s攻撃力")
    private val defenceTranslation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}.defence" }, "%s Defence", "%s防御力")
    override fun getText(value: Value): Component {
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

            val attacker = source.entity
            if (attacker is PlayerEntity) {
                var attackBonus = 0.0
                attacker.passiveSkillResult.getOrCreate()[ElementPassiveSkillEffect].attackMap.forEach { (element, value) ->
                    if (element.test(source)) {
                        attackBonus += value
                    }
                }
                damage *= (1.0 + attackBonus).toFloat()
            }

            if (entity is PlayerEntity) {
                var defenceBonus = 0.0
                entity.passiveSkillResult.getOrCreate()[ElementPassiveSkillEffect].defenceMap.forEach { (element, value) ->
                    if (element.test(source)) {
                        defenceBonus += value
                    }
                }
                damage /= (1.0 + defenceBonus).toFloat()
            }

            damage
        }

        DamageTypes.CACTUS.location().registerDamageTypeTagGeneration { SPINE_DAMAGE_TYPE_TAG }
        DamageTypes.SWEET_BERRY_BUSH.location().registerDamageTypeTagGeneration { SPINE_DAMAGE_TYPE_TAG }
        DamageTypes.STING.location().registerDamageTypeTagGeneration { SPINE_DAMAGE_TYPE_TAG }
        DamageTypes.THORNS.location().registerDamageTypeTagGeneration { SPINE_DAMAGE_TYPE_TAG }
    }
}
