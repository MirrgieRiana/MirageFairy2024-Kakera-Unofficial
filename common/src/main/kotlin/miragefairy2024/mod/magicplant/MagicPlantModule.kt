package miragefairy2024.mod.magicplant

import com.mojang.brigadier.arguments.StringArgumentType
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import dev.architectury.event.events.common.CommandRegistrationEvent
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.magicplant.contents.initTraitCard
import miragefairy2024.mod.magicplant.contents.initTraitConditionCard
import miragefairy2024.mod.magicplant.contents.initTraitEffectKeyCard
import miragefairy2024.mod.magicplant.contents.magicplants.DiamondLuminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.EmeraldLuminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.GoldProminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.MerrrriaCard
import miragefairy2024.mod.magicplant.contents.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.PhantomFlowerCard
import miragefairy2024.mod.magicplant.contents.magicplants.ProminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.SarraceniaCard
import miragefairy2024.mod.magicplant.contents.magicplants.TopazLuminariaCard
import miragefairy2024.mod.magicplant.contents.magicplants.VeropedaCard
import miragefairy2024.mod.magicplant.contents.magicplants.XarpaLuminariaCard
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.Registration
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.executesThrowable
import miragefairy2024.util.failure
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.string
import miragefairy2024.util.success
import miragefairy2024.util.text
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.writeAction
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.join
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.level.block.Blocks

val magicPlantCards: List<MagicPlantCard<*>> = listOf(
    MirageFlowerCard,
    PhantomFlowerCard,
    VeropedaCard,
    SarraceniaCard,
    DiamondLuminariaCard,
    TopazLuminariaCard,
    EmeraldLuminariaCard,
    XarpaLuminariaCard,
    ProminariaCard,
    GoldProminariaCard,
    MerrrriaCard,
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


    initTraitListScreenHandler()
    initTraitConditionCard()
    initTraitEffectKeyCard()
    initTraitCard()
    initCreativeGeneAmpoule()

    magicPlantCards.forEach { card ->
        card.init()
    }

    registerCommand()

    registerClientDebugItem("dump_magic_plant_environments", Blocks.OAK_SAPLING.toTextureSource(), 0xFF00FF00.toInt()) { _, player, _, _ ->
        val lines = mutableListOf<String>()
        magicPlantCards.groupBy { it.family }.forEach { (_, cards) ->

            val temperatureTraitConditions = listOf(TemperatureCategory.LOW, TemperatureCategory.MEDIUM, TemperatureCategory.HIGH)
            val humidityTraitConditions = listOf(HumidityCategory.HIGH, HumidityCategory.MEDIUM, HumidityCategory.LOW)

            fun f(t: Int, h: Int): String {
                return cards
                    .filter { it.hasEnvironmentAdaptation(true, temperatureTraitConditions[t], humidityTraitConditions[h]) }
                    .join("&br;") { it.blockName.ja }
            }

            lines += "** ${cards.first().classification.ja}"
            lines += ""
            lines += "|BGCOLOR(#f8edff):|BGCOLOR(#f8edff):||||c"
            lines += "|>||>|>|CENTER:温度|h"
            lines += "|>|~|CENTER:低温|CENTER:中温|CENTER:高温|h"
            lines += "|CENTER:湿度|CENTER:湿潤|${f(0, 0)}|${f(1, 0)}|${f(2, 0)}|"
            lines += "|~|CENTER:中湿|${f(0, 1)}|${f(1, 1)}|${f(2, 1)}|"
            lines += "|~|CENTER:乾燥|${f(0, 2)}|${f(1, 2)}|${f(2, 2)}|"
            lines += ""
        }
        writeAction(player, "magic_plant_environments.txt", lines.join("") { "$it\n" })
    }

    registerClientDebugItem("dump_magic_plant_traits", Blocks.OAK_SAPLING.toTextureSource(), 0xFF00FFFF.toInt()) { _, player, _, _ ->
        val lines = mutableListOf<String>()

        lines += "|${(listOf("特性", "条件", "効果") + magicPlantCards.map { it.blockName.ja.chunked(1).join("&br;") }).join("|")}|h"

        TraitCard.entries.forEach { traitCard ->
            val row = mutableListOf<String>()
            row += traitCard.jaName
            row += traitCard.trait.conditions.join("&br;") { it.name.string }
            row += traitCard.trait.traitEffectKeyEntries.join("&br;") { it.traitEffectKey.name.string }
            row += magicPlantCards.map { magicPlantCard ->
                when (traitCard.trait) {
                    in magicPlantCard.defaultTraitBits -> "○"
                    in magicPlantCard.randomTraitChances -> "△"
                    else -> ""
                }
            }
            lines += "|${row.join("|")}|"
        }

        writeAction(player, "magic_plant_traits.txt", lines.join("") { "$it\n" })
    }

}

private fun registerCommand() {
    ClientCommandRegistrationEvent.EVENT.register { dispatcher, _ ->
        dispatcher.register(
            Commands.literal("mf24ku") // TODO -> common
                .then(
                    Commands.literal("trait")
                        .then(
                            Commands.literal("negative")
                                .then(
                                    Commands.argument("trait", ResourceLocationArgument.id())
                                        .suggests { _, builder ->
                                            traitRegistry.keySet().forEach {
                                                builder.suggest(it.string)
                                            }
                                            builder.buildFuture()
                                        }
                                        .executesThrowable { context ->
                                            val player = context.source.playerOrException
                                            val id = ResourceLocationArgument.getId(context, "trait")
                                            val trait = traitRegistry.get(id) ?: failure(text { "Unknown trait: $id"() })
                                            NegativeTraitBitsRegistry.set(trait, null)
                                            player.sendSystemMessage(text { "Set negative trait bits for ${trait.getIdentifier()} = ALL"().yellow })
                                            success()
                                        }
                                        .then(
                                            Commands.argument("bits", StringArgumentType.word())
                                                .suggests { _, builder ->
                                                    listOf("1111", "0000", "1010", "10110010").forEach {
                                                        builder.suggest(it)
                                                    }
                                                    builder.buildFuture()
                                                }
                                                .executesThrowable { context ->
                                                    val player = context.source.playerOrException
                                                    val id = ResourceLocationArgument.getId(context, "trait")
                                                    val trait = traitRegistry.get(id) ?: failure(text { "Unknown trait: $id"() })
                                                    val rawBits = StringArgumentType.getString(context, "bits")
                                                    if (!rawBits.matches("""^[01_]{1,32}$""".toRegex())) failure(text { "Invalid bits format"() })
                                                    val mask = rawBits.replace("_", "").fold(0) { acc, c -> (acc shl 1) or (c - '0') }
                                                    NegativeTraitBitsRegistry.set(trait, mask)
                                                    player.sendSystemMessage(text { "Set negative trait bits for ${trait.getIdentifier()} = $rawBits"().yellow })
                                                    success()
                                                }
                                        )
                                )
                        )
                )
        )
    }
}
