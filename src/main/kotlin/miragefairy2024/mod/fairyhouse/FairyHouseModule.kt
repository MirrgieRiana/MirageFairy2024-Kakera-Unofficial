package miragefairy2024.mod.fairyhouse

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import net.minecraft.util.Identifier

enum class FairyHouseModelCard(val identifier: Identifier) {
    LANTERN ( Identifier(MirageFairy2024.modId, "block/fairy_house/lantern")),
}

context(ModContext)
fun initFairyHouseModule() {
    FairyHouseCard.init()
    FairyCollectorCard.init()
}
