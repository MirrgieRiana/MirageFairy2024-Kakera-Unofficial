package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.Translation
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.ceilToInt
import mirrg.kotlin.hydrogen.max
import net.minecraft.core.Direction
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

fun <T : ToolConfiguration> T.areaMining(horizontal: Int, front: Int, back: Int) = this.merge(AreaMiningToolEffectType, AreaMiningToolEffectType.Value(horizontal, front, back))

object AreaMiningToolEffectType : ToolEffectType<ToolConfiguration, AreaMiningToolEffectType.Value> {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.area_mining" }, "Area mining %s %s %s", "範囲採掘 %s %s %s")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    class Value(val horizontal: Int, val front: Int, val back: Int)

    override fun castOrThrow(value: Any?) = value as Value
    override fun merge(a: Value, b: Value) = Value(a.horizontal max b.horizontal, a.front max b.front, a.back max b.back)

    override fun apply(configuration: ToolConfiguration, value: Value) {
        if (value.horizontal <= 0 && value.front <= 0 && value.back <= 0) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION(value.horizontal, value.front, value.back) })
        configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (world.isClientSide) return@fail

            if (miner.isShiftKeyDown) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!item.isCorrectToolForDrops(stack, state)) return@fail // 掘ったブロックに対して特効でない
            val breakDirection = breakDirectionCache[miner.uuid] ?: return@fail // 向きの判定が不正

            // 発動

            val baseHardness = state.getDestroySpeed(world, pos)

            val h = value.horizontal
            val f = value.front
            val b = value.back
            val (xRange, yRange, zRange) = when (breakDirection) {
                Direction.DOWN -> Triple(-h..h, -b..f, -h..h)
                Direction.UP -> Triple(-h..h, -f..b, -h..h)
                Direction.NORTH -> Triple(-h..h, -h..h, -b..f)
                Direction.SOUTH -> Triple(-h..h, -h..h, -f..b)
                Direction.WEST -> Triple(-b..f, -h..h, -h..h)
                Direction.EAST -> Triple(-f..b, -h..h, -h..h)
            }

            // TODO 貫通抑制
            xRange.forEach { x ->
                yRange.forEach { y ->
                    zRange.forEach { z ->
                        if (x != 0 || y != 0 || z != 0) {
                            val targetBlockPos = pos.offset(x, y, z)
                            if (item.isCorrectToolForDrops(stack, world.getBlockState(targetBlockPos))) run skip@{
                                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                                if (stack.maxDamage - stack.damageValue <= configuration.magicMiningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                                // 採掘を続行

                                val targetBlockState = world.getBlockState(targetBlockPos)
                                val targetHardness = targetBlockState.getDestroySpeed(world, targetBlockPos)
                                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                                if (breakBlockByMagic(stack, world, targetBlockPos, miner)) {
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
            }
        }
    }
}
