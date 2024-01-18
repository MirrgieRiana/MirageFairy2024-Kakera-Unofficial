package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.mirageFairy2024ItemGroup
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemModelGeneration
import miragefairy2024.util.string
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object FairyCard {
    val enName = "Invalid Fairy"
    val jaName = "無効な妖精"
    val identifier = Identifier(MirageFairy2024.modId, "fairy")
    val item = FairyItem(Item.Settings())
}

fun initFairyModule() {
    FairyCard.let { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(mirageFairy2024ItemGroup)

        card.item.registerItemModelGeneration(createFairyModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            when (tintIndex) {
                0 -> 0xFFEEDD // skin
                1 -> 0xAAAAFF // back
                2 -> 0xAAAAFF // front
                3 -> 0x440000 // hair
                4 -> 0xAA0000 // dress
                else -> 0x000000
            }
        }

        card.item.enJa(card.enName, card.jaName)
    }
}

private fun createFairyModel() = Model {
    ModelData(
        parent = Identifier("item/generated"),
        textures = ModelTexturesData(
            "layer0" to Identifier(MirageFairy2024.modId, "item/fairy_skin").string,
            "layer1" to Identifier(MirageFairy2024.modId, "item/fairy_back").string,
            "layer2" to Identifier(MirageFairy2024.modId, "item/fairy_front").string,
            "layer3" to Identifier(MirageFairy2024.modId, "item/fairy_hair").string,
            "layer4" to Identifier(MirageFairy2024.modId, "item/fairy_dress").string,
        ),
    )
}

class FairyItem(settings: Settings) : Item(settings) {

}
