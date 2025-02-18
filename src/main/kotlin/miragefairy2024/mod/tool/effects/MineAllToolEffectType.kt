package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.ceilToInt
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.minecraft.entity.EquipmentSlot
import net.minecraft.server.network.ServerPlayerEntity

fun ToolConfiguration.mineAll() = this.also {
    this.merge(MineAllToolEffectType, MineAllToolEffectType.Value(true)) { value ->
        MineAllToolEffectType.apply(this, value)
    }
    it.descriptions += text { ToolConfiguration.MINE_ALL_TRANSLATION() }
}

object MineAllToolEffectType : ToolEffectType<MineAllToolEffectType.Value> {
    class Value(val enabled: Boolean)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.enabled || b.enabled)
    fun apply(configuration: ToolConfiguration, value: Value) {
        configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (world.isClient) return@fail

            if (miner.isSneaking) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!item.isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない
            if (!state.isIn(ConventionalBlockTags.ORES)) return@fail // 掘ったブロックが鉱石ではない

            // 発動

            val baseHardness = state.getHardness(world, pos)

            blockVisitor(listOf(pos), visitOrigins = false, maxDistance = 19, maxCount = 31) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).block === state.block
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
                }
            }
        }
    }
}
