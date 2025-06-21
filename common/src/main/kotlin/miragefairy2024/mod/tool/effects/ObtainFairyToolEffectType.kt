package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.fairy.FairyDreamRecipes
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.fairyHistoryContainer
import miragefairy2024.mod.fairy.getRandomFairy
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.mutate
import miragefairy2024.util.text
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

fun <T : ToolConfiguration> T.obtainFairy(appearanceRateBonus: Double) = this.also {
    this.merge(ObtainFairyToolEffectType, appearanceRateBonus) { appearanceRateBonus ->
        ObtainFairyToolEffectType.apply(this, appearanceRateBonus)
    }
}

object ObtainFairyToolEffectType : DoubleAddToolEffectType() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.obtain_fairy" }, "Obtain a fairy when mined or killed", "採掘・撃破時に妖精を入手")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    fun apply(configuration: ToolConfiguration, appearanceRateBonus: Double) {
        if (appearanceRateBonus <= 0.0) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.onAfterBreakBlockListeners += fail@{ _, world, player, pos, state, _, _ ->
            if (player !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない

            // モチーフの判定
            val motifSet = FairyDreamRecipes.BLOCK.test(state.block)

            // 抽選
            val result = getRandomFairy(world.random, motifSet, appearanceRateBonus) ?: return@fail

            // 入手
            val fairyItemStack = result.motif.createFairyItemStack(condensation = result.condensation, count = result.count)
            world.addFreshEntity(ItemEntity(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, fairyItemStack))

            // 妖精召喚履歴に追加
            player.fairyHistoryContainer.mutate { it[result.motif] += result.condensation * result.count }

        }
        configuration.onKilledListeners += fail@{ _, entity, attacker, _ ->
            if (attacker !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない

            // モチーフの判定
            val motifSet = FairyDreamRecipes.ENTITY_TYPE.test(entity.type)

            // 抽選
            val result = getRandomFairy(entity.level().random, motifSet, appearanceRateBonus) ?: return@fail

            // 入手
            val fairyItemStack = result.motif.createFairyItemStack(condensation = result.condensation, count = result.count)
            entity.level().addFreshEntity(ItemEntity(entity.level(), entity.x, entity.y, entity.z, fairyItemStack))

            // 妖精召喚履歴に追加
            attacker.fairyHistoryContainer.mutate { it[result.motif] += result.condensation * result.count }

        }
    }
}
