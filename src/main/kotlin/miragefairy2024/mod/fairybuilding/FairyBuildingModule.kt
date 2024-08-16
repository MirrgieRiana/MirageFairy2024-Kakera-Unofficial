package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import net.minecraft.util.Identifier

enum class FairyBuildingModelCard(val identifier: Identifier) {
    LANTERN(MirageFairy2024.identifier("block/fairy_building/lantern")),
    LANTERN_OFF(MirageFairy2024.identifier("block/fairy_building/lantern_off")),
}

val fairyBuildingCards: List<FairyBuildingCard<*, *, *>> = listOf(
    FairyHouseCard,
    FairyCollectorCard,
)

val FOLIA_TRANSLATION = Translation({ "gui.${MirageFairy2024.modId}.fairy_building.folia" }, "Folia", "フォリア")
val SPECIFIED_FAIRY_SLOT_TRANSLATION = Translation({ "gui.${MirageFairy2024.modId}.fairy_building.specified_fairy_slot" }, "Only %s Family", "%s系統のみ")

context(ModContext)
fun initFairyBuildingModule() {
    fairyBuildingCards.forEach { card ->
        card.init()
    }

    FOLIA_TRANSLATION.enJa()
    SPECIFIED_FAIRY_SLOT_TRANSLATION.enJa()
}
