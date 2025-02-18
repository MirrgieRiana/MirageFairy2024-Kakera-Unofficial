package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration

fun ToolConfiguration.glint() = this.also {
    this.merge(GlintToolEffectType, true) { enabled ->
        GlintToolEffectType.apply(this, enabled)
    }
}

object GlintToolEffectType : BooleanToolEffectType() {
    fun apply(configuration: ToolConfiguration, enabled: Boolean) {
        if (!enabled) return
        configuration.hasGlint = true
    }
}
