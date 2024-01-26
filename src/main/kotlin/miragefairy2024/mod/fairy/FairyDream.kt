package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import net.minecraft.util.Identifier

val GAIN_FAIRY_DREAM_TRANSLATION = Translation({ "gui.miragefairy2024.fairy_dream.gain" }, "Dreamed of a new fairy!", "新たな妖精の夢を見た！")

fun initFairyDream() {

    FairyDreamContainerExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, Identifier(MirageFairy2024.modId, "fairy_dream"))

    GAIN_FAIRY_DREAM_TRANSLATION.enJa()

}
