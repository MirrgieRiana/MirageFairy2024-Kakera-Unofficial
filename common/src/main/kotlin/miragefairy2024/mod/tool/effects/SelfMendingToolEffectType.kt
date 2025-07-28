package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.repair
import miragefairy2024.util.text
import net.minecraft.world.entity.player.Player

fun <T : ToolConfiguration> T.selfMending(speed: Int) = this.merge(SelfMendingToolEffectType, speed)

object SelfMendingToolEffectType : IntAddToolEffectType<ToolConfiguration>() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toLanguageKey()}.self_mending" }, "Self-mending while held", "手に持っている間、自己修繕")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    override fun apply(configuration: ToolConfiguration, value: Int) {
        if (value <= 0) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.onInventoryTickListeners += fail@{ _, stack, world, entity, _, _ ->
            if (world.isClientSide) return@fail
            if (entity !is Player) return@fail // プレイヤーじゃない
            if (stack !== entity.mainHandItem && stack !== entity.offhandItem) return@fail // 手に持っていない
            stack.repair(world.random.randomInt(1.0 / 60.0 / 20.0) * value)
        }
    }
}
