package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.Translation
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.ceilToInt
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

fun ToolConfiguration.mineAll() = this.also {
    this.merge(MineAllToolEffectType, true) { enabled ->
        MineAllToolEffectType.apply(this, enabled)
    }
}

object MineAllToolEffectType : BooleanToolEffectType() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    fun apply(configuration: ToolConfiguration, enabled: Boolean) {
        if (!enabled) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (world.isClientSide) return@fail

            if (miner.isShiftKeyDown) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!item.isCorrectToolForDrops(stack, state)) return@fail // 掘ったブロックに対して特効でない
            if (!state.`is`(ConventionalBlockTags.ORES)) return@fail // 掘ったブロックが鉱石ではない

            // 発動

            val baseHardness = state.getDestroySpeed(world, pos)

            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 31) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).block === state.block
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damageValue <= configuration.magicMiningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getDestroySpeed(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        val damage = world.random.randomInt(configuration.magicMiningDamage)
                        if (damage > 0) {
                            stack.hurtAndBreak(damage, miner, EquipmentSlot.MAINHAND)
                        }
                    }
                }
            }
        }
    }
}
