package miragefairy2024.mod.fairy

import miragefairy2024.ModEvents

fun initFairyModule() = ModEvents.onInitialize {
    initFairyItem()
    initFairyCondensationRecipe()
    initMotif()
    initMotifTableScreenHandler()
    initRandomFairySummoning()
    initFairyDream()
    initFairyDreamContainer()
    initSoulStream()
}
