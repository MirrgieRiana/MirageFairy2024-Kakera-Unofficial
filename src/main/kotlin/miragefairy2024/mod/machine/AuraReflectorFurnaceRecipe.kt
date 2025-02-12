package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.createItemStack
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier

object AuraReflectorFurnaceRecipeCard : SimpleMachineRecipeCard<AuraReflectorFurnaceRecipe>() {
    override val identifier = MirageFairy2024.identifier("aura_reflector_furnace")
    override val icon = AuraReflectorFurnaceCard.item.createItemStack()
    override val recipeClass = AuraReflectorFurnaceRecipe::class.java
    override fun createRecipe(recipeId: Identifier, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): AuraReflectorFurnaceRecipe {
        return AuraReflectorFurnaceRecipe(this, recipeId, group, inputs, output, duration)
    }
}

class AuraReflectorFurnaceRecipe(
    card: AuraReflectorFurnaceRecipeCard,
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
