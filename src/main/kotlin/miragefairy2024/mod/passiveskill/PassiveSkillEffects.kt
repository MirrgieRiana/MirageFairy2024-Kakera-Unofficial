package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.register
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.entity.attribute.EntityAttributes

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

abstract class DoublePassiveSkillEffectCard(path: String) : PassiveSkillEffectCard<Double>(path) {
    override val unit = 0.0
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
}
