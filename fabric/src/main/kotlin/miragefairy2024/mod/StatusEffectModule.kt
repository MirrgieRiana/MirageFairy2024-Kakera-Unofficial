package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.en
import miragefairy2024.util.isServer
import miragefairy2024.util.ja
import miragefairy2024.util.registerStatic
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.world.entity.player.Player as PlayerEntity

val experienceStatusEffectHolder = ExperienceStatusEffect().registerStatic(BuiltInRegistries.MOB_EFFECT, MirageFairy2024.identifier("experience"))

context(ModContext)
fun initStatusEffectModule() {
    en { experienceStatusEffectHolder.value().descriptionId to "Experience" }
    ja { experienceStatusEffectHolder.value().descriptionId to "経験値獲得" }
}

class ExperienceStatusEffect : MobEffect(MobEffectCategory.BENEFICIAL, 0x2FFF00) {
    override fun shouldApplyEffectTickThisTick(duration: Int, amplifier: Int) = true
    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        super.applyEffectTick(entity, amplifier)
        val world = entity.level()
        if (world.gameTime % 5 != 0L) return true
        if (world.isServer && entity is PlayerEntity) {
            entity.giveExperiencePoints(1 + amplifier)
            world.playSound(null, entity.x, entity.y, entity.z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, (world.random.nextFloat() - world.random.nextFloat()) * 0.35F + 0.9F)
        }
        return true
    }
}
