package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.NeighborType
import miragefairy2024.util.Translation
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.ceilToInt
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

fun ToolConfiguration.cutAll() = this.also {
    this.merge(CutAllToolEffectType, true) { enabled ->
        CutAllToolEffectType.apply(this, enabled)
    }
}

object CutAllToolEffectType : BooleanToolEffectType() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.cut_all" }, "Cut down the entire tree", "木全体を伐採")

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
            if (!state.`is`(BlockTags.LOGS)) return@fail // 掘ったブロックが原木ではない

            // 発動

            val baseHardness = state.getDestroySpeed(world, pos)

            val logBlockPosList = mutableListOf<BlockPos>()
            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 19, neighborType = NeighborType.VERTICES) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).`is`(BlockTags.LOGS)
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damageValue <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getDestroySpeed(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        val damage = world.random.randomInt(configuration.miningDamage)
                        if (damage > 0) {
                            stack.hurtAndBreak(damage, miner, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND))
                        }
                    }
                    logBlockPosList += blockPos
                }
            }
            blockVisitor(logBlockPosList, visitOrigins = false, maxDistance = 8) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).`is`(BlockTags.LEAVES)
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damageValue <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getDestroySpeed(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        if (miner.random.nextFloat() < 0.1F) {
                            val damage = world.random.randomInt(configuration.miningDamage)
                            if (damage > 0) {
                                stack.hurtAndBreak(damage, miner, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND))
                            }
                        }
                    }
                }
            }
        }
    }
}
