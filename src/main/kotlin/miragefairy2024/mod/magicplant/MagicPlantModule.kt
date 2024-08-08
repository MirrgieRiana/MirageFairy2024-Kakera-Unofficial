package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.initTraitCard
import miragefairy2024.mod.magicplant.contents.initTraitEffectKeyCard
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerSettings
import miragefairy2024.mod.magicplant.contents.magicplants.VeropedaSettings
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import net.minecraft.util.Identifier

val TRAIT_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.trait" }, "Trait", "特性")
val CREATIVE_ONLY_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.creativeOnly" }, "Creative Only", "クリエイティブ専用")
val INVALID_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.invalid" }, "Invalid", "無効")

val magicPlantSeedItemGroupCard = ItemGroupCard(
    Identifier(MirageFairy2024.modId, "magic_plant_seeds"), "Magic Plant Seeds", "魔法植物の種子",
) { MirageFlowerCard.item.createItemStack() }

context(ModContext)
fun initMagicPlantModule() {

    TRAIT_TRANSLATION.enJa()
    CREATIVE_ONLY_TRANSLATION.enJa()
    INVALID_TRANSLATION.enJa()

    magicPlantSeedItemGroupCard.init()


    initTraitListScreenHandler()
    initTraitEffectKeyCard()
    initTraitCard()

    MirageFlowerSettings.init()
    VeropedaSettings.init()

}
