package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.world.entity.ExperienceOrb as ExperienceOrbEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.AABB as Box

fun ToolConfiguration.collection() = this.also {
    this.merge(CollectionToolEffectType, true) { enabled ->
        CollectionToolEffectType.apply(this, enabled)
    }
}

object CollectionToolEffectType : BooleanToolEffectType() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toTranslationKey()}.collection" }, "Collect drop items when mined or killed", "採掘・撃破時にドロップ品を回収")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    fun apply(configuration: ToolConfiguration, enabled: Boolean) {
        if (!enabled) return
        configuration.descriptions += text { TRANSLATION() }
        configuration.onAfterBreakBlockListeners += fail@{ _, world, player, pos, _, _, _ ->
            if (player.world != world) return@fail
            world.getEntitiesByClass(ItemEntity::class.java, Box(pos)) { !it.isSpectator }.forEach {
                it.teleport(player.x, player.y, player.z)
                it.resetPickupDelay()
            }
            world.getEntitiesByClass(ExperienceOrbEntity::class.java, Box(pos)) { !it.isSpectator }.forEach {
                it.teleport(player.x, player.y, player.z)
            }
        }
        configuration.onKilledListeners += fail@{ _, entity, attacker, _ ->
            if (attacker.world != entity.world) return@fail
            entity.world.getEntitiesByClass(ItemEntity::class.java, entity.boundingBox) { !it.isSpectator }.forEach {
                it.teleport(attacker.x, attacker.y, attacker.z)
                it.resetPickupDelay()
            }
            entity.world.getEntitiesByClass(ExperienceOrbEntity::class.java, entity.boundingBox) { !it.isSpectator }.forEach {
                it.teleport(attacker.x, attacker.y, attacker.z)
            }
        }
    }
}
