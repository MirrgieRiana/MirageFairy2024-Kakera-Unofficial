package miragefairy2024.mod.passiveskill.conditions

import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import net.minecraft.world.effect.MobEffect as StatusEffect
import net.minecraft.network.chat.Component as Text

class StatusEffectPassiveSkillCondition(private val statusEffect: StatusEffect) : PassiveSkillCondition {
    override val text: Text get() = statusEffect.displayName
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.hasStatusEffect(statusEffect)
}
