package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import net.minecraft.util.Identifier

enum class FairyBuildingModelCard(val identifier: Identifier) {
    LANTERN(Identifier(MirageFairy2024.modId, "block/fairy_building/lantern")),
    LANTERN_OFF(Identifier(MirageFairy2024.modId, "block/fairy_building/lantern_off")),
}

val fairyBuildingCards: List<FairyBuildingCard<*, *>> = listOf(
    FairyHouseCard,
    FairyCollectorCard,
)

val FOLIA_TRANSLATION = Translation({ "gui.${MirageFairy2024.modId}.fairy_building.folia" }, "Folia", "フォリア")

context(ModContext)
fun initFairyBuildingModule() {
    fairyBuildingCards.forEach { card ->
        card.init()
    }

    FOLIA_TRANSLATION.enJa()
}
