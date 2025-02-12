package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.createItemStack
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier

object FermentationBarrelRecipeCard : SimpleMachineRecipeCard<FermentationBarrelRecipe>() {
    override val identifier = MirageFairy2024.identifier("fermentation_barrel")
    override val icon = FermentationBarrelCard.item.createItemStack()
    override fun createRecipe(identifier: Identifier, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): FermentationBarrelRecipe {
        return FermentationBarrelRecipe(this, identifier, group, inputs, output, duration)
    }
}

class FermentationBarrelRecipe(
    card: FermentationBarrelRecipeCard,
    identifier: Identifier,
    group: String,
    inputs: List<Pair<Ingredient, Int>>,
    output: ItemStack,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    identifier,
    group,
    inputs,
    output,
    duration,
)
