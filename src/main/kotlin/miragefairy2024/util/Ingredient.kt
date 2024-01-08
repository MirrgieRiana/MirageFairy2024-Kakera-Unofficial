package miragefairy2024.util

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.tag.TagKey

fun Item.toIngredient(): Ingredient = Ingredient.ofItems(this)
fun ItemStack.toIngredient(): Ingredient = Ingredient.ofStacks(this)
fun TagKey<Item>.toIngredient(): Ingredient = Ingredient.fromTag(this)
