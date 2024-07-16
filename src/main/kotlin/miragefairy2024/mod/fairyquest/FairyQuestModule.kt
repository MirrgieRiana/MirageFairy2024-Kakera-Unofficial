package miragefairy2024.mod.fairyquest

import miragefairy2024.ModEvents

fun initFairyQuestModule() = ModEvents.onInitialize {
    initFairyQuestCardScreenHandler()
    initFairyQuestCardItem()
    initFairyQuestRecipe()
}
