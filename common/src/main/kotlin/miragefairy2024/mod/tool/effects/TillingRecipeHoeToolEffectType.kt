package miragefairy2024.mod.tool.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.TextPoem
import miragefairy2024.mod.tool.ToolEffectType
import miragefairy2024.mod.tool.items.FairyHoeConfiguration
import miragefairy2024.mod.tool.items.TillingRecipe
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text

fun <T : FairyHoeConfiguration> T.tillingRecipe(tillingRecipe: TillingRecipe) = this.also {
    this.merge(TillingRecipeHoeToolEffectType, tillingRecipe) { tillingRecipe ->
        TillingRecipeHoeToolEffectType.apply(this, tillingRecipe)
    }
}

object TillingRecipeHoeToolEffectType : ToolEffectType<TillingRecipe> {
    private val TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("fairy_hoe").toLanguageKey()}.tilling_recipe" }, "Special tilling effects", "特殊な耕作効果")

    context(ModContext)
    fun init() {
        TRANSLATION.enJa()
    }

    fun apply(configuration: FairyHoeConfiguration, tillingRecipe: TillingRecipe) {
        configuration.descriptions += TextPoem(PoemType.DESCRIPTION, text { TRANSLATION() })
        configuration.tillingRecipe = tillingRecipe
    }

    override fun castOrThrow(value: Any?) = value as TillingRecipe
    override fun merge(a: TillingRecipe, b: TillingRecipe) = throw AssertionError("Merging TillingRecipe is not supported")
}
