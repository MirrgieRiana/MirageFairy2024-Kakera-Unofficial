package miragefairy2024.mod.rei

import com.google.gson.JsonObject
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.fermentationbarrel.FermentationBarrelRecipe
import miragefairy2024.mod.rei.FairyFamilyReiCategoryCard.Display
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import mirrg.kotlin.gson.hydrogen.toJsonElement
import mirrg.kotlin.hydrogen.Single

object FermentationBarrelReiCategoryCard : ReiCategoryCard<FermentationBarrelReiCategoryCard.Display>("fermentation_barrel", "Fermentation Barrel", "発酵樽") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            val id = tag.wrapper["id"].string.get()!!
            val json = tag.wrapper["json"].string.get()!!
            Display(FermentationBarrelRecipe.Serializer.read(id.toIdentifier(), json.toJsonElement() as JsonObject))
        }, { display, tag ->
            val jsonObject = JsonObject()
            FermentationBarrelRecipe.Serializer.write(jsonObject, display.recipe)
            tag.wrapper["id"].string.set(display.recipe.identifier.string)
            tag.wrapper["json"].string.set(jsonObject.toString())
        }))
    }

    class Display(val recipe: FermentationBarrelRecipe) : BasicDisplay(
        listOf(
            recipe.input1.first.matchingStacks.map { it.copyWithCount(recipe.input1.second).toEntryStack() }.toEntryIngredient(),
            recipe.input2.first.matchingStacks.map { it.copyWithCount(recipe.input2.second).toEntryStack() }.toEntryIngredient(),
            recipe.input3.first.matchingStacks.map { it.copyWithCount(recipe.input3.second).toEntryStack() }.toEntryIngredient(),
        ),
        listOf(
            recipe.output.toEntryStack().toEntryIngredient(),
        ),
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
