package miragefairy2024.mod.passiveskill.conditions

import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffect

class StatusEffectPassiveSkillCondition(private val statusEffect: MobEffect) : PassiveSkillCondition {
    override val text: Component get() = statusEffect.displayName
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.hasEffect(statusEffect)
}
