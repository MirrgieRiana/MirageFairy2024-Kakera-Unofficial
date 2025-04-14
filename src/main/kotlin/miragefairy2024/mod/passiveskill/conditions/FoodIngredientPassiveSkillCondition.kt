package miragefairy2024.mod.passiveskill.conditions

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.FoodIngredientCategory
import miragefairy2024.mod.containsAsFoodIngredient
import miragefairy2024.mod.lastFood
import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import miragefairy2024.util.orEmpty
import miragefairy2024.util.text
import net.minecraft.world.item.Item
import java.time.Instant

class ItemFoodIngredientPassiveSkillCondition(private val item: Item) : PassiveSkillCondition {
    companion object {
        val identifier = MirageFairy2024.identifier("food_ingredient")
        val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_condition.${identifier.toLanguageKey()}" }, "%s Dishes", "%s料理")
    }

    override val text get() = text { translation(item.description) }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        if (!(context.player.lastFood.itemStack.orEmpty.item containsAsFoodIngredient item)) return false
        val time = context.player.lastFood.time ?: return false
        val now = Instant.now()
        return time in now.minusSeconds(3600 * 25)..now
    }
}

class CategoryFoodIngredientPassiveSkillCondition(private val category: FoodIngredientCategory) : PassiveSkillCondition {
    override val text get() = text { ItemFoodIngredientPassiveSkillCondition.translation(category.text) }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean {
        if (!(context.player.lastFood.itemStack.orEmpty.item containsAsFoodIngredient category)) return false
        val time = context.player.lastFood.time ?: return false
        val now = Instant.now()
        return time in now.minusSeconds(3600 * 25)..now
    }
}
