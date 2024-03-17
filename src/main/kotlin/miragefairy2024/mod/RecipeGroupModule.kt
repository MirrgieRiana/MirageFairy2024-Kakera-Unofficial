package miragefairy2024.mod

import net.minecraft.item.Item
import net.minecraft.item.Items

val recipeGroupRegistry = mutableMapOf<Item, String>()

fun initRecipeGroupModule() {
    recipeGroupRegistry[Items.STICK] = "sticks"
}
