package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.initTraitCard
import miragefairy2024.mod.magicplant.contents.initTraitConditionCard
import miragefairy2024.mod.magicplant.contents.initTraitEffectKeyCard
import miragefairy2024.mod.magicplant.contents.magicplants.DiamondLuminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.EmeraldLuminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.PhantomFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.SarraceniaCard
import miragefairy2024.mod.magicplant.contents.magicplants.VeropedaCard
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.util.ExtraCodecs

val magicPlantCards: List<MagicPlantCard<*>> = listOf(
    MirageFlowerCard,
    PhantomFlowerCard,
    VeropedaCard,
    SarraceniaCard,
    DiamondLuminariaCard,
    EmeraldLuminariaCard,
)

private val identifier = MirageFairy2024.identifier("magic_plant")
val TRAIT_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.trait" }, "Trait", "特性")
val CREATIVE_ONLY_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.creativeOnly" }, "Creative Only", "クリエイティブ専用")
val GUI_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.gui" }, "Use while sneaking to show traits", "スニーク中に使用時、特性GUIを表示")
val INVALID_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.invalid" }, "Invalid", "無効")

val TRAIT_STACKS_DATA_COMPONENT_TYPE: DataComponentType<TraitStacks> = DataComponentType.builder<TraitStacks>()
    .persistent(TraitStacks.CODEC)
    .networkSynchronized(TraitStacks.STREAM_CODEC)
    .cacheEncoding()
    .build()

val RARITY_DATA_COMPONENT_TYPE: DataComponentType<Int> = DataComponentType.builder<Int>()
    .persistent(ExtraCodecs.intRange(0, 1))
    .networkSynchronized(ByteBufCodecs.VAR_INT)
    .build()

context(ModContext)
fun initMagicPlantModule() {

    Registration(BuiltInRegistries.DATA_COMPONENT_TYPE, MirageFairy2024.identifier("trait_stacks")) { TRAIT_STACKS_DATA_COMPONENT_TYPE }.register()
    Registration(BuiltInRegistries.DATA_COMPONENT_TYPE, MirageFairy2024.identifier("rarity")) { RARITY_DATA_COMPONENT_TYPE }.register()

    TRAIT_TRANSLATION.enJa()
    CREATIVE_ONLY_TRANSLATION.enJa()
    GUI_TRANSLATION.enJa()
    INVALID_TRANSLATION.enJa()


    initTraitSpawnConditionScope()
    initTraitListScreenHandler()
    initTraitConditionCard()
    initTraitEffectKeyCard()
    initTraitCard()
    initCreativeGeneAmpoule()

    magicPlantCards.forEach { card ->
        card.init()
    }

}
