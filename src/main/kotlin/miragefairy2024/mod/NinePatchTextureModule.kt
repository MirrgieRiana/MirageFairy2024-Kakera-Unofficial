package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.MirageFairy2024DataGenerator
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
        val SLOT = NinePatchTextureCard("slot", 16, 16, 5, 5, false)

        val entries = listOf(SLOT)
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    val texture = "textures/gui/" concat identifier concat ".png"
}

fun initNinePatchTextureModule() {
    NinePatchTextureCard.entries.forEach { card ->
        MirageFairy2024DataGenerator.ninePatchTextureGenerators {
            it(card.identifier, card)
        }
    }
}
