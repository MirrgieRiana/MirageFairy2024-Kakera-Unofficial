package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.text
import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.randomInt
import miragefairy2024.util.repair
import miragefairy2024.util.text
import net.minecraft.entity.player.PlayerEntity

fun ToolConfiguration.selfMending(speed: Int) = this.also {
    this.merge(SelfMendingToolEffectType, speed) { speed ->
        SelfMendingToolEffectType.apply(this, speed)
    }
}

object SelfMendingToolEffectType : IntToolEffectType() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_mining_tool").toTranslationKey()}.self_mending" }, "Self-mending while in the main hand", "メインハンドにある間、自己修繕")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    fun apply(configuration: ToolConfiguration, speed: Int) {
        if (speed <= 0) return
        configuration.onAddPoemListeners += { _, poemList ->
            poemList.text(PoemType.DESCRIPTION, text { TRANSLATION() })
        }
        configuration.onInventoryTickListeners += fail@{ _, stack, world, entity, _, _ ->
            if (world.isClient) return@fail
            if (entity !is PlayerEntity) return@fail // プレイヤーじゃない
            if (stack !== entity.mainHandStack) return@fail // メインハンドに持っていない
            stack.repair(world.random.randomInt(1.0 / 60.0 / 20.0) * speed)
        }
    }
}
