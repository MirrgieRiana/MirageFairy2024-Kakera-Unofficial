package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.BaseStoneType.DEEPSLATE
import miragefairy2024.mod.BaseStoneType.STONE
import miragefairy2024.util.Model
import miragefairy2024.util.concat
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerOreLootTableGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerTagGeneration
import miragefairy2024.util.string
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.ExperienceDroppingBlock
import net.minecraft.block.MapColor
import net.minecraft.block.enums.Instrument
import net.minecraft.data.client.TextureKey
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.structure.rule.TagMatchRuleTest
import net.minecraft.util.Identifier
import net.minecraft.util.math.intprovider.UniformIntProvider
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.YOffset
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.OreFeatureConfig
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier

enum class BaseStoneType {
    STONE,
    DEEPSLATE,
}

enum class OreCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList,
    val baseStoneType: BaseStoneType,
    texturePath: String,
    val dropItem: Item,
    experience: Pair<Int, Int>,
) {
    MIRANAGITE_ORE(
        "miranagite_ore", "Miranagite Ore", "蒼天石鉱石",
        PoemList(1).poem("What lies beyond a Garden of Eden?", "秩序の石は楽園の先に何を見るのか？"),
        STONE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    DEEPSLATE_MIRANAGITE_ORE(
        "deepslate_miranagite_ore", "Deepslate Miranagite Ore", "深層蒼天石鉱石",
        PoemList(1).poem("Singularities built by the Creator", "楽園が楽園であるための奇跡。"),
        DEEPSLATE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val block = run {
        val settings = when (baseStoneType) {
            STONE -> FabricBlockSettings.create()
                .mapColor(MapColor.STONE_GRAY)
                .instrument(Instrument.BASEDRUM)
                .requiresTool()
                .strength(3.0F, 3.0F)

            DEEPSLATE -> FabricBlockSettings.create()
                .mapColor(MapColor.DEEPSLATE_GRAY)
                .instrument(Instrument.BASEDRUM)
                .requiresTool()
                .strength(4.5F, 3.0F)
                .sounds(BlockSoundGroup.DEEPSLATE)
        }
        ExperienceDroppingBlock(settings, UniformIntProvider.create(experience.first, experience.second))
    }
    val item = BlockItem(block, Item.Settings())
    val texturedModel = run {
        val baseStoneTexture = when (baseStoneType) {
            STONE -> Identifier("minecraft", "block/stone")
            DEEPSLATE -> Identifier("minecraft", "block/deepslate")
        }
        OreModelCard.model.with(
            TextureKey.BACK to baseStoneTexture,
            TextureKey.FRONT to ("block/" concat Identifier(MirageFairy2024.modId, texturePath)),
        )
    }
}

object OreModelCard {
    val parentModel = createOreModel()
    val identifier = Identifier(MirageFairy2024.modId, "block/ore")
    val model = Model(identifier, TextureKey.BACK, TextureKey.FRONT)
}

fun initOresModule() {

    OreModelCard.parentModel.registerModelGeneration(OreModelCard.identifier)

    OreCard.entries.forEach { card ->
        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroup)

        card.block.registerSingletonBlockStateGeneration()
        card.block.registerModelGeneration(card.texturedModel)
        card.block.registerCutoutRenderLayer()

        card.block.enJa(card.enName, card.jaName)
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.block.registerOreLootTableGeneration(card.dropItem)

        card.block.registerTagGeneration(BlockTags.PICKAXE_MINEABLE)
        card.block.registerTagGeneration(BlockTags.NEEDS_STONE_TOOL)

    }

    fun worldGen(card: OreCard) {

        val configuredKey = registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, card.identifier) {
            val targets = when (card.baseStoneType) {
                STONE -> listOf(OreFeatureConfig.createTarget(TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES), card.block.defaultState))
                DEEPSLATE -> listOf(OreFeatureConfig.createTarget(TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), card.block.defaultState))
            }
            Feature.ORE with OreFeatureConfig(targets, 12)
        }

        registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, card.identifier) {
            val placementModifiers = listOf(
                CountPlacementModifier.of(8),
                SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(128)),
                BiomePlacementModifier.of(),
            )
            it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(configuredKey) with placementModifiers
        }.also {
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, it)
        }

    }
    worldGen(OreCard.MIRANAGITE_ORE)
    worldGen(OreCard.DEEPSLATE_MIRANAGITE_ORE)

}

fun createOreModel() = Model {
    jsonObject(
        "parent" to Identifier("minecraft", "block/block").string.jsonElement,
        "textures" to jsonObject(
            TextureKey.PARTICLE.name to TextureKey.BACK.string.jsonElement,
        ),
        "elements" to jsonArray(
            jsonObject(
                "from" to jsonArray(0.jsonElement, 0.jsonElement, 0.jsonElement),
                "to" to jsonArray(16.jsonElement, 16.jsonElement, 16.jsonElement),
                "faces" to jsonObject(
                    "down" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "down".jsonElement),
                    "up" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "up".jsonElement),
                    "north" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "north".jsonElement),
                    "south" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "south".jsonElement),
                    "west" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "west".jsonElement),
                    "east" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "east".jsonElement),
                ),
            ),
            jsonObject(
                "from" to jsonArray(0.jsonElement, 0.jsonElement, 0.jsonElement),
                "to" to jsonArray(16.jsonElement, 16.jsonElement, 16.jsonElement),
                "faces" to jsonObject(
                    "down" to jsonObject("texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "down".jsonElement),
                    "up" to jsonObject("texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "up".jsonElement),
                    "north" to jsonObject("texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "north".jsonElement),
                    "south" to jsonObject("texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "south".jsonElement),
                    "west" to jsonObject("texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "west".jsonElement),
                    "east" to jsonObject("texture" to TextureKey.FRONT.string.jsonElement, "cullface" to "east".jsonElement),
                ),
            ),
        ),
    )
}
