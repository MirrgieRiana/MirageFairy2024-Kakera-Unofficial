package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.mirageFairy2024ItemGroup
import miragefairy2024.util.Model
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemModelGeneration
import miragefairy2024.util.string
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import kotlin.random.Random
import kotlin.random.nextInt

object FairyQuestCardCard {
    val enName = "Fairy Quest Card"
    val jaName = "フェアリークエストカード"
    val identifier = Identifier(MirageFairy2024.modId, "fairy_quest_card")
    val item = Item(Item.Settings())
}

fun initFairyQuestCardItem() {
    FairyQuestCardCard.let { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(mirageFairy2024ItemGroup)
        card.item.registerItemModelGeneration(createFairyQuestCardModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            when (tintIndex) {
                0 -> {
                    // TODO
                    val random = Random(itemStack.count)
                    repeat(10) {
                        random.nextInt()
                    }
                    (random.nextInt(0x80..0xFF) shl 16) or (random.nextInt(0x80..0xFF) shl 8) or random.nextInt(0x80..0xFF)
                }

                else -> 0xFFFFFF
            }
        }
        card.item.enJa(card.enName, card.jaName)
    }
}

private fun createFairyQuestCardModel() = Model {
    jsonObject(
        "parent" to Identifier("item/generated").string.jsonElement,
        "textures" to jsonObject(
            "layer0" to Identifier(MirageFairy2024.modId, "item/fairy_quest_card_background").string.jsonElement,
            "layer1" to Identifier(MirageFairy2024.modId, "item/fairy_quest_card_white").string.jsonElement,
            "layer2" to Identifier(MirageFairy2024.modId, "item/fairy_quest_card_frame").string.jsonElement,
        ),
    )
}
