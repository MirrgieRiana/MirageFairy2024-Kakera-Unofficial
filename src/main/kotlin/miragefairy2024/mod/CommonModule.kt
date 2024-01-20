package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.createItemStack
import net.minecraft.util.Identifier

val mirageFairy2024ItemGroupCard = ItemGroupCard(
    Identifier(MirageFairy2024.modId, "miragefairy2024"), "MirageFairy2024", "MirageFairy2024",
) { MaterialCard.FAIRY_PLASTIC.item.createItemStack() }

fun initCommonModule() {
    mirageFairy2024ItemGroupCard.init()
}
