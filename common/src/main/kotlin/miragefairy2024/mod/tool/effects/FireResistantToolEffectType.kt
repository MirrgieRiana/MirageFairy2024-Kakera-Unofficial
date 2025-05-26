package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration

fun ToolConfiguration.fireResistant() = this.also {
    this.merge(FireResistantToolEffectType, true) { enabled ->
        FireResistantToolEffectType.apply(this, enabled)
    }
}

object FireResistantToolEffectType : BooleanToolEffectType() {
    fun apply(configuration: ToolConfiguration, enabled: Boolean) {
        if (!enabled) return
        configuration.fireResistant = true
    }
}
