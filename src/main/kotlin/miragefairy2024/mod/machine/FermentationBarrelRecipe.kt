package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.createItemStack
import miragefairy2024.util.isNotEmpty
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier

object FermentationBarrelRecipeCard : SimpleMachineRecipeCard<FermentationBarrelRecipe>() {
    override val identifier = MirageFairy2024.identifier("fermentation_barrel")
    override val icon = FermentationBarrelCard.item.createItemStack()
    override val recipeClass = FermentationBarrelRecipe::class.java
    override fun createRecipe(recipeIdentifier: Identifier, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): FermentationBarrelRecipe {
        return FermentationBarrelRecipe(this, recipeIdentifier, group, inputs, output, duration)
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
) {
    override fun getCustomizedRemainder(itemStack: ItemStack): ItemStack {
        val remainder = super.getCustomizedRemainder(itemStack)
        if (remainder.isNotEmpty) return remainder

        if (itemStack.isOf(Items.POTION)) return Items.GLASS_BOTTLE.createItemStack()

        return EMPTY_ITEM_STACK
    }
}
