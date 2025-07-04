package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge

fun <T : ToolConfiguration> T.fireResistant() = this.merge(FireResistantToolEffectType, true)

object FireResistantToolEffectType : BooleanToolEffectType<ToolConfiguration>() {
    override fun apply(configuration: ToolConfiguration, value: Boolean) {
        if (!value) return
        configuration.fireResistant = true
    }
}
