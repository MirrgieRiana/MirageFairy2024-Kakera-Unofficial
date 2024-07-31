package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import net.minecraft.util.Identifier

enum class FairyBuildingModelCard(val identifier: Identifier) {
    LANTERN(Identifier(MirageFairy2024.modId, "block/fairy_building/lantern")),
}

val fairyBuildingCards: List<FairyBuildingCard<*, *>> = listOf(
    FairyHouseCard,
    FairyCollectorCard,
)

context(ModContext)
fun initFairyBuildingModule() {
    fairyBuildingCards.forEach { card ->
        card.init()
    }
}
