package miragefairy2024.mod.rei

import miragefairy2024.util.enJa

fun initReiModule() {
    ReiCategoryCard.entries.forEach { card ->
        card.translation.enJa()
    }
}
