package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.tags.TagKey

fun Item.toIngredient(): Ingredient = Ingredient.ofItems(this)
fun ItemStack.toIngredient(): Ingredient = Ingredient.ofStacks(this)
fun TagKey<Item>.toIngredient(): Ingredient = Ingredient.fromTag(this)

context(ModContext)
fun CustomIngredientSerializer<*>.register() {
    ModEvents.onInitialize {
        CustomIngredientSerializer.register(this)
    }
}
