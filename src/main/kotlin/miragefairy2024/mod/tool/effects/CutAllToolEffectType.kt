package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.ceilToInt
import net.minecraft.entity.EquipmentSlot
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos

fun ToolConfiguration.cutAll() = this.also {
    this.merge(CutAllToolEffectType, CutAllToolEffectType.Value(true)) { value ->
        CutAllToolEffectType.apply(this, value)
    }
    it.descriptions += text { ToolConfiguration.CUT_ALL_TRANSLATION() }
}

object CutAllToolEffectType : ToolEffectType<CutAllToolEffectType.Value> {
    class Value(val enabled: Boolean)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.enabled || b.enabled)
    fun apply(configuration: ToolConfiguration, value: Value) {
        configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (world.isClient) return@fail

            if (miner.isSneaking) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!item.isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
            if (!state.isIn(BlockTags.LOGS)) return@fail // 掘ったブロックが原木ではない

            // 発動

            val baseHardness = state.getHardness(world, pos)

            val logBlockPosList = mutableListOf<BlockPos>()
            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 19, neighborType = NeighborType.VERTICES) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).isIn(BlockTags.LOGS)
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getHardness(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        val damage = world.random.randomInt(configuration.miningDamage)
                        if (damage > 0) {
                            stack.damage(damage, miner) {
                                it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                            }
                        }
                    }
                    logBlockPosList += blockPos
                }
            }
            blockVisitor(logBlockPosList, visitOrigins = false, maxDistance = 8) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).isIn(BlockTags.LEAVES)
            }.forEach skip@{ (_, blockPos) ->
                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                if (stack.maxDamage - stack.damage <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                // 採掘を続行

                val targetBlockState = world.getBlockState(blockPos)
                val targetHardness = targetBlockState.getHardness(world, blockPos)
                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                if (breakBlockByMagic(stack, world, blockPos, miner)) {
                    if (targetHardness > 0) {
                        if (miner.random.nextFloat() < 0.1F) {
                            val damage = world.random.randomInt(configuration.miningDamage)
                            if (damage > 0) {
                                stack.damage(damage, miner) {
                                    it.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
