package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import mirrg.kotlin.hydrogen.Single

object TraitReiCategoryCard : ReiCategoryCard<TraitReiCategoryCard.Display>("trait", "Trait", "特性") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display()
        }, { display, tag ->

        }))
    }

    class Display : BasicDisplay(listOf(), listOf()) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
