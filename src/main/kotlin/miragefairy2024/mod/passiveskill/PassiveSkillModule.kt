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
import miragefairy2024.util.enJa
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.entity.attribute.EntityAttributes
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
