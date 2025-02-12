package miragefairy2024.mod.rei

import com.google.gson.JsonObject
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.machine.AuraReflectorFurnaceCard
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipe
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipeCard
import miragefairy2024.mod.machine.FermentationBarrelCard
import miragefairy2024.mod.machine.FermentationBarrelRecipe
import miragefairy2024.mod.machine.FermentationBarrelRecipeCard
import miragefairy2024.mod.machine.SimpleMachineRecipe
import miragefairy2024.mod.machine.SimpleMachineRecipeCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import mirrg.kotlin.gson.hydrogen.toJsonElement
import mirrg.kotlin.hydrogen.Single
import net.minecraft.item.ItemStack

abstract class SimpleMachineReiCategoryCard<R : SimpleMachineRecipe>(path: String, enName: String, jaName: String) : ReiCategoryCard<SimpleMachineReiCategoryCard.Display<R>>(path, enName, jaName) {
    override val serializer: Single<BasicDisplay.Serializer<Display<R>>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            val id = tag.wrapper["id"].string.get()!!
            val json = tag.wrapper["json"].string.get()!!
            Display(this, recipeCard.serializer.read(id.toIdentifier(), json.toJsonElement() as JsonObject))
        }, { display, tag ->
            val jsonObject = JsonObject()
            recipeCard.serializer.write(jsonObject, display.recipe)
            tag.wrapper["id"].string.set(display.recipe.recipeId.string)
            tag.wrapper["json"].string.set(jsonObject.toString())
        }))
    }

    abstract val recipeCard: SimpleMachineRecipeCard<R>
    abstract val machine: ItemStack

    open fun getInputIndices(recipe: R): List<Int> = recipe.inputs.indices.toList()
    open fun getInputs(recipe: R): List<EntryIngredient> {
        return recipe.inputs.map { input ->
            input.first.matchingStacks.map { it.copyWithCount(input.second).toEntryStack() }.toEntryIngredient()
        }
    }

    class Display<R : SimpleMachineRecipe>(private val card: SimpleMachineReiCategoryCard<R>, val recipe: R) : BasicDisplay(
        card.getInputs(recipe),
        listOf(
            recipe.output.toEntryStack().toEntryIngredient(),
        ),
    ) {
        override fun getCategoryIdentifier() = card.identifier.first
    }
}

object FermentationBarrelReiCategoryCard : SimpleMachineReiCategoryCard<FermentationBarrelRecipe>("fermentation_barrel", "Fermentation Barrel", "醸造樽") {
    override val recipeCard = FermentationBarrelRecipeCard
    override val machine = FermentationBarrelCard.item.createItemStack()
}

object AuraReflectorFurnaceReiCategoryCard : SimpleMachineReiCategoryCard<AuraReflectorFurnaceRecipe>("aura_reflector_furnace", "Aura Reflector Furnace", "オーラ反射炉") {
    override val recipeCard = AuraReflectorFurnaceRecipeCard
    override val machine = AuraReflectorFurnaceCard.item.createItemStack()

    fun getFuelInputIndices(recipe: AuraReflectorFurnaceRecipe) = listOf(recipe.inputs.size)
    override fun getInputs(recipe: AuraReflectorFurnaceRecipe): List<EntryIngredient> {
        return super.getInputs(recipe) + listOf(AuraReflectorFurnaceRecipe.FUELS.map { it.createItemStack().toEntryStack() }.toEntryIngredient())
    }
}
