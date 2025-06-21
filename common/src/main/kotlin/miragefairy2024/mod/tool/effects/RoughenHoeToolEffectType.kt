package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.items.FairyHoeConfiguration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text

fun <T : FairyHoeConfiguration> T.roughen() = this.also {
    this.merge(RoughenHoeToolEffectType, true) { enabled ->
        RoughenHoeToolEffectType.apply(this, enabled)
    }
}

object RoughenHoeToolEffectType : BooleanToolEffectType() {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_hoe").toLanguageKey()}.roughen" }, "Roughen the ground", "地面を荒らす")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    fun apply(configuration: FairyHoeConfiguration, enabled: Boolean) {
        if (!enabled) return
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.roughen = true
    }
}
