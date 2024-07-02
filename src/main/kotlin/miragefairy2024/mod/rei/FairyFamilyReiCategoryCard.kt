package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay

object FairyFamilyReiCategoryCard : ReiCategoryCard<FairyFamilyReiCategoryCard.Display>("fairy_family", "Fairy Family", "妖精系統") {
    override val serializer: BasicDisplay.Serializer<Display> by lazy {
        BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display() // TODO
        }, { display, tag ->
            // TODO
        })
    }

    class Display : BasicDisplay(listOf(), listOf()) {
        override fun getCategoryIdentifier() = identifier
    }
}
