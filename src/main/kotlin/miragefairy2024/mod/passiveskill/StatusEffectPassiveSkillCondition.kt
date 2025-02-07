package miragefairy2024.mod.passiveskill

import net.minecraft.entity.effect.StatusEffect
import net.minecraft.text.Text

class StatusEffectPassiveSkillCondition(private val statusEffect: StatusEffect) : PassiveSkillCondition {
    override val text: Text get() = statusEffect.name
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.hasStatusEffect(statusEffect)
}
