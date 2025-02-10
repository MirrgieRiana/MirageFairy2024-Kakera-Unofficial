package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.passiveskill.conditions.ItemFoodIngredientPassiveSkillCondition
import miragefairy2024.mod.passiveskill.conditions.SimplePassiveSkillConditionCard
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
import miragefairy2024.util.boolean
import miragefairy2024.util.double
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.register
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey

val passiveSkillEffectRegistryKey: RegistryKey<Registry<PassiveSkillEffect<*>>> = RegistryKey.ofRegistry(MirageFairy2024.identifier("passive_skill_effect"))
val passiveSkillEffectRegistry: Registry<PassiveSkillEffect<*>> = FabricRegistryBuilder.createSimple(passiveSkillEffectRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

context(ModContext)
fun initPassiveSkillModule() {

    // Condition

    SimplePassiveSkillConditionCard.entries.forEach { card ->
        card.init()
    }
    ItemFoodIngredientPassiveSkillCondition.translation.enJa()


    // Effect

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


    // Execution

    initPassiveSkillExecution()

}

abstract class AbstractPassiveSkillEffect<T>(path: String) : PassiveSkillEffect<T> {
    val identifier = MirageFairy2024.identifier(path)

    context(ModContext)
    open fun init() {
        this.register(passiveSkillEffectRegistry, identifier)
    }

    override val isPreprocessor = false
}

abstract class AbstractDoublePassiveSkillEffect(path: String) : AbstractPassiveSkillEffect<Double>(path) {
    override val unit = 0.0
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
    override fun fromNbt(nbt: NbtCompound) = nbt.wrapper["value"].double.get()!!
    override fun toNbt(value: Double) = NbtCompound().also { it.wrapper["value"].double.set(value) }
}

abstract class AbstractBooleanPassiveSkillEffect(path: String) : AbstractPassiveSkillEffect<Boolean>(path) {
    override val unit = false
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun combine(a: Boolean, b: Boolean) = a || b
    override fun fromNbt(nbt: NbtCompound) = nbt.wrapper["value"].boolean.get()!!
    override fun toNbt(value: Boolean) = NbtCompound().also { it.wrapper["value"].boolean.set(value) }
}
