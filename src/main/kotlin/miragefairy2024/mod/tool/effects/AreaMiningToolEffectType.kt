package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.Translation
import miragefairy2024.util.breakBlockByMagic
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.hydrogen.ceilToInt
import net.minecraft.entity.EquipmentSlot
import net.minecraft.server.network.ServerPlayerEntity

fun ToolConfiguration.areaMining(level: Int = 1) = this.also {
    this.merge(AreaMiningToolEffectType, level) { level ->
        AreaMiningToolEffectType.apply(this, level)
    }
}

object AreaMiningToolEffectType : IntMaxToolEffectType() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toTranslationKey()}.area_mining" }, "Area mining %s", "範囲採掘 %s")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    fun apply(configuration: ToolConfiguration, level: Int) {
        if (level <= 0) return
        configuration.descriptions += text { TRANSLATION(level.toRomanText()) }
        configuration.onPostMineListeners += fail@{ item, stack, world, state, pos, miner ->
            if (world.isClient) return@fail

            if (miner.isSneaking) return@fail // 使用者がスニーク中
            if (miner !is ServerPlayerEntity) return@fail // 使用者がプレイヤーでない
            if (!item.isSuitableFor(state)) return@fail // 掘ったブロックに対して特効でない

            // 発動

            val baseHardness = state.getHardness(world, pos)

            // TODO 貫通抑制
            (-level..level).forEach { x ->
                (-level..level).forEach { y ->
                    (-level..level).forEach { z ->
                        if (x != 0 || y != 0 || z != 0) {
                            val targetBlockPos = pos.add(x, y, z)
                            if (item.isSuitableFor(world.getBlockState(targetBlockPos))) run skip@{
                                if (stack.isEmpty) return@fail // ツールの耐久値が枯渇した
                                if (stack.maxDamage - stack.damage <= configuration.miningDamage.ceilToInt()) return@fail // ツールの耐久値が残り僅か

                                // 採掘を続行

                                val targetBlockState = world.getBlockState(targetBlockPos)
                                val targetHardness = targetBlockState.getHardness(world, targetBlockPos)
                                if (targetHardness > baseHardness) return@skip // 起点のブロックよりも硬いものは掘れない
                                if (breakBlockByMagic(stack, world, targetBlockPos, miner)) {
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
            }
        }
    }
}
