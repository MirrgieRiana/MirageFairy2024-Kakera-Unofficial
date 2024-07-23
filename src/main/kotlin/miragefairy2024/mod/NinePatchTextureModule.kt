package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.concat
import net.minecraft.util.Identifier

class NinePatchTextureCard(
    path: String,
    val textureWidth: Int,
    val textureHeight: Int,
    val patchWidth: Int,
    val patchHeight: Int,
    val repeat: Boolean,
) {
    companion object {
        val FAIRY_QUEST_CARD_MESSAGE = NinePatchTextureCard("fairy_quest_card_message", 66, 66, 22, 22, true)

        val entries = listOf(FAIRY_QUEST_CARD_MESSAGE)
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    val texture = "textures/gui/" concat identifier concat ".png"
}

context(ModContext)
fun initNinePatchTextureModule() = ModEvents.onInitialize {
    NinePatchTextureCard.entries.forEach { card ->
        MirageFairy2024DataGenerator.ninePatchTextureGenerators {
            it(card.identifier, card)
        }
    }
}
