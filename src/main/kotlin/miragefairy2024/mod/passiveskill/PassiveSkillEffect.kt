package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.passiveskill.effects.CollectionPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ElementPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.EntityAttributePassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ExperiencePassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.HungerPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.IgnitionPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.ManaBoostPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.MendingPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.MiningSpeedPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.RegenerationPassiveSkillEffect
import miragefairy2024.mod.passiveskill.effects.StatusEffectPassiveSkillEffect
import miragefairy2024.util.register
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.entity.attribute.EntityAttributes

context(ModContext)
fun initPassiveSkillEffect() {
    listOf(
        ManaBoostPassiveSkillEffect,
        EntityAttributePassiveSkillEffect,
        StatusEffectPassiveSkillEffect,
        IgnitionPassiveSkillEffect,
        ExperiencePassiveSkillEffect,
        RegenerationPassiveSkillEffect,
        HungerPassiveSkillEffect,
        MendingPassiveSkillEffect,
        CollectionPassiveSkillEffect,
        ElementPassiveSkillEffect,
        MiningSpeedPassiveSkillEffect,
    ).forEach { card ->
        card.init()
    }

    ModEvents.onInitialize {
        EntityAttributePassiveSkillEffect.FORMATTERS[EntityAttributes.GENERIC_MOVEMENT_SPEED] = { (it / 0.1) * 100 formatAs "%+.0f%%" }
    }
}

abstract class PassiveSkillEffectCard<T>(path: String) : PassiveSkillEffect<T> {
    val identifier = MirageFairy2024.identifier(path)

    context(ModContext)
    open fun init() {
        this.register(passiveSkillEffectRegistry, identifier)
    }

    override val isPreprocessor = false
}

abstract class DoublePassiveSkillEffectCard(path: String) : PassiveSkillEffectCard<Double>(path) {
    override val unit = 0.0
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
}

abstract class BooleanPassiveSkillEffectCard(path: String) : PassiveSkillEffectCard<Boolean>(path) {
    override val unit = false
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun combine(a: Boolean, b: Boolean) = a || b
}
