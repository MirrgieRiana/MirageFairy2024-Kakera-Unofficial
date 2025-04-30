package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items


// api

object FoodIngredientsRegistry {
    val registry = mutableMapOf<Item, FoodIngredients>()
}

class FoodIngredients(val items: List<Item> = listOf(), val categories: List<FoodIngredientCategory> = listOf()) {
    val allItems by lazy {
        val result = mutableListOf<Item>()
        fun f(foodIngredients: FoodIngredients) {
            foodIngredients.items.forEach {
                result += it
            }
            foodIngredients.items.forEach {
                val subFoodIngredients = FoodIngredientsRegistry.registry[it]
                if (subFoodIngredients != null) f(subFoodIngredients)
            }
        }
        f(this)
        result
    }
    val allCategories by lazy {
        val result = mutableListOf<FoodIngredientCategory>()
        fun f(foodIngredients: FoodIngredients) {
            foodIngredients.categories.forEach {
                result += it
            }
            foodIngredients.items.forEach {
                val subFoodIngredients = FoodIngredientsRegistry.registry[it]
                if (subFoodIngredients != null) f(subFoodIngredients)
            }
        }
        f(this)
        result
    }
}

interface FoodIngredientCategory {
    val text: Component
}


// util

operator fun FoodIngredients.plus(item: Item) = FoodIngredients(this.items + item, this.categories)
operator fun FoodIngredients.plus(category: FoodIngredientCategory) = FoodIngredients(this.items, this.categories + category)

infix fun Item.containsAsFoodIngredient(ingredient: Item): Boolean {
    if (this == ingredient) return true
    val foodIngredients = FoodIngredientsRegistry.registry[this] ?: return false
    return ingredient in foodIngredients.allItems
}

infix fun Item.containsAsFoodIngredient(ingredient: FoodIngredientCategory): Boolean {
    val foodIngredients = FoodIngredientsRegistry.registry[this] ?: return false
    return ingredient in foodIngredients.allCategories
}


// impl

enum class FoodIngredientCategoryCard(path: String, enName: String, jaName: String) : FoodIngredientCategory {
    FISH("fish", "Fish", "魚"),
    MUSHROOM("mushroom", "Mushroom", "キノコ"),
    ALCOHOL("alcohol", "Alcohol", "アルコール")
    ;

    val translation = Translation({ "${MirageFairy2024.MOD_ID}.food_ingredient.$path" }, enName, jaName)
    override val text get() = text { translation() }
}

context(ModContext)
fun initFoodIngredientsModule() {
    FoodIngredientCategoryCard.entries.forEach { card ->
        card.translation.enJa()
    }

    ModEvents.onInitialize {
        FoodIngredientsRegistry.registry[Items.GOLDEN_APPLE] = FoodIngredients() + Items.APPLE
        FoodIngredientsRegistry.registry[Items.ENCHANTED_GOLDEN_APPLE] = FoodIngredients() + Items.GOLDEN_APPLE
        FoodIngredientsRegistry.registry[Items.GOLDEN_CARROT] = FoodIngredients() + Items.CARROT
        FoodIngredientsRegistry.registry[Items.BAKED_POTATO] = FoodIngredients() + Items.POTATO
        FoodIngredientsRegistry.registry[Items.POISONOUS_POTATO] = FoodIngredients() + Items.POTATO
        FoodIngredientsRegistry.registry[Items.DRIED_KELP] = FoodIngredients() + Items.KELP
        FoodIngredientsRegistry.registry[Items.COOKED_BEEF] = FoodIngredients() + Items.BEEF
        FoodIngredientsRegistry.registry[Items.COOKED_PORKCHOP] = FoodIngredients() + Items.PORKCHOP
        FoodIngredientsRegistry.registry[Items.COOKED_MUTTON] = FoodIngredients() + Items.MUTTON
        FoodIngredientsRegistry.registry[Items.COOKED_CHICKEN] = FoodIngredients() + Items.CHICKEN
        FoodIngredientsRegistry.registry[Items.COOKED_RABBIT] = FoodIngredients() + Items.RABBIT
        FoodIngredientsRegistry.registry[Items.COD] = FoodIngredients() + FoodIngredientCategoryCard.FISH
        FoodIngredientsRegistry.registry[Items.COOKED_COD] = FoodIngredients() + Items.COD
        FoodIngredientsRegistry.registry[Items.SALMON] = FoodIngredients() + FoodIngredientCategoryCard.FISH
        FoodIngredientsRegistry.registry[Items.COOKED_SALMON] = FoodIngredients() + Items.SALMON
        FoodIngredientsRegistry.registry[Items.BREAD] = FoodIngredients() + Items.WHEAT
        FoodIngredientsRegistry.registry[Items.COOKIE] = FoodIngredients() + Items.WHEAT + Items.COCOA_BEANS
        FoodIngredientsRegistry.registry[Items.PUMPKIN_PIE] = FoodIngredients() + Items.PUMPKIN + Items.SUGAR + Items.EGG
        FoodIngredientsRegistry.registry[Items.MUSHROOM_STEW] = FoodIngredients() + Items.RED_MUSHROOM + Items.BROWN_MUSHROOM
        FoodIngredientsRegistry.registry[Items.BEETROOT_SOUP] = FoodIngredients() + Items.BEETROOT
        FoodIngredientsRegistry.registry[Items.RABBIT_STEW] = FoodIngredients() + Items.RABBIT + Items.POTATO + Items.CARROT + FoodIngredientCategoryCard.MUSHROOM
        FoodIngredientsRegistry.registry[Items.SUSPICIOUS_STEW] = FoodIngredients() + Items.RED_MUSHROOM + Items.BROWN_MUSHROOM
        FoodIngredientsRegistry.registry[Items.RED_MUSHROOM] = FoodIngredients() + FoodIngredientCategoryCard.MUSHROOM
        FoodIngredientsRegistry.registry[Items.BROWN_MUSHROOM] = FoodIngredients() + FoodIngredientCategoryCard.MUSHROOM
    }
}
