package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.register
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.InstantStatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier

val experienceStatusEffect = ExperienceStatusEffect()

fun initStatusEffectModule() {
    experienceStatusEffect.register(Registries.STATUS_EFFECT, Identifier(MirageFairy2024.modId, "experience"))
    en { experienceStatusEffect.translationKey to "Experience" }
    ja { experienceStatusEffect.translationKey to "経験値獲得" }
}

class ExperienceStatusEffect : InstantStatusEffect(StatusEffectCategory.BENEFICIAL, 0x2FFF00) {
    override fun applyUpdateEffect(entity: LivingEntity, amplifier: Int) {
        super.applyUpdateEffect(entity, amplifier)
        val world = entity.world
        if (world.time % 5 != 0L) return
        if (!world.isClient && entity is PlayerEntity) {
            entity.addExperience(1 + amplifier)
            world.playSound(null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, (world.random.nextFloat() - world.random.nextFloat()) * 0.35F + 0.9F)
        }
    }
}
