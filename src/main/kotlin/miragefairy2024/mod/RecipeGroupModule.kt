package miragefairy2024.mod

import miragefairy2024.ModEvents

import net.minecraft.item.Item
import net.minecraft.item.Items

val recipeGroupRegistry = mutableMapOf<Item, String>()

fun initRecipeGroupModule() = ModEvents.onInitialize {
    recipeGroupRegistry[Items.STICK] = "sticks"
}
