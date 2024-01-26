package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.util.register
import net.minecraft.util.Identifier

fun initFairyDream() {

    FairyDreamContainerExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, Identifier(MirageFairy2024.modId, "fairy_dream"))

}
