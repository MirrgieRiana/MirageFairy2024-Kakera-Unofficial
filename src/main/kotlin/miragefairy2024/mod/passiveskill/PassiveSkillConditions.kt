package miragefairy2024.mod.passiveskill

import miragefairy2024.ModContext
import miragefairy2024.util.enJa

context(ModContext)
fun initPassiveSkillConditions() {
    SimplePassiveSkillConditionCard.entries.forEach { card ->
        card.init()
    }
    ItemFoodIngredientPassiveSkillCondition.translation.enJa()
    MainHandConditionCard.entries.forEach { card ->
        card.translation.enJa()
    }
}
