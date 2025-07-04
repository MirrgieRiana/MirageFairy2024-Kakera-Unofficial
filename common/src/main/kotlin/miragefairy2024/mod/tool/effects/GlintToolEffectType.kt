package miragefairy2024.mod.tool.effects

import miragefairy2024.mod.tool.ToolConfiguration
import miragefairy2024.mod.tool.merge

fun <T : ToolConfiguration> T.glint() = this.merge(GlintToolEffectType, true)

object GlintToolEffectType : BooleanToolEffectType<ToolConfiguration>() {
    override fun apply(configuration: ToolConfiguration, value: Boolean) {
        if (!value) return
        configuration.hasGlint = true
    }
}
