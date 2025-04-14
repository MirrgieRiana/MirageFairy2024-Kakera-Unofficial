package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.overworld
import miragefairy2024.util.placementModifiers
import miragefairy2024.util.randomIntCount
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerFeature
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerOreLootTableGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.uniformOre
import miragefairy2024.util.with
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags
import net.minecraft.world.level.block.DropExperienceBlock as ExperienceDroppingBlock
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument as Instrument
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest as TagMatchRuleTest
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.util.valueproviders.UniformInt as UniformIntProvider
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration as OreFeatureConfig

enum class BaseStoneType {
    STONE,
    DEEPSLATE,
}

enum class OreCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList?,
    val baseStoneType: BaseStoneType,
    texturePath: String,
    val dropItem: Item,
    experience: Pair<Int, Int>,
) {
    MAGNETITE_ORE(
        "magnetite_ore", "Magnetite Ore", "磁鉄鉱鉱石",
        null,
        BaseStoneType.STONE, "magnetite_ore", MaterialCard.MAGNETITE.item, 2 to 5,
    ),
    DEEPSLATE_MAGNETITE_ORE(
        "deepslate_magnetite_ore", "Deepslate Magnetite Ore", "深層磁鉄鉱鉱石",
        null,
        BaseStoneType.DEEPSLATE, "magnetite_ore", MaterialCard.MAGNETITE.item, 2 to 5,
    ),
    FLUORITE_ORE(
        "fluorite_ore", "Fluorite Ore", "蛍石鉱石",
        null,
        BaseStoneType.STONE, "fluorite_ore", MaterialCard.FLUORITE.item, 2 to 5,
    ),
    DEEPSLATE_FLUORITE_ORE(
        "deepslate_fluorite_ore", "Deepslate Fluorite Ore", "深層蛍石鉱石",
        null,
        BaseStoneType.DEEPSLATE, "fluorite_ore", MaterialCard.FLUORITE.item, 2 to 5,
    ),
    MIRANAGITE_ORE(
        "miranagite_ore", "Miranagite Ore", "蒼天石鉱石",
        PoemList(1).poem("What lies beyond a Garden of Eden?", "秩序の石は楽園の先に何を見るのか？"),
        BaseStoneType.STONE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    DEEPSLATE_MIRANAGITE_ORE(
        "deepslate_miranagite_ore", "Deepslate Miranagite Ore", "深層蒼天石鉱石",
        PoemList(1).poem("Singularities built by the Creator", "楽園が楽園であるための奇跡。"),
        BaseStoneType.DEEPSLATE, "miranagite_ore", MaterialCard.MIRANAGITE.item, 2 to 5,
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val block = run {
        val settings = when (baseStoneType) {
            BaseStoneType.STONE -> FabricBlockSettings.create()
                .mapColor(MapColor.STONE_GRAY)
                .instrument(Instrument.BASEDRUM)
                .requiresTool()
                .strength(3.0F, 3.0F)

            BaseStoneType.DEEPSLATE -> FabricBlockSettings.create()
                .mapColor(MapColor.DEEPSLATE_GRAY)
                .instrument(Instrument.BASEDRUM)
                .requiresTool()
                .strength(4.5F, 3.0F)
                .sounds(BlockSoundGroup.DEEPSLATE)
        }
        ExperienceDroppingBlock(settings, UniformIntProvider.create(experience.first, experience.second))
    }
    val item = BlockItem(block, Item.Properties())
    val texturedModelFactory = TexturedModel.Factory {
        val baseStoneTexture = when (baseStoneType) {
            BaseStoneType.STONE -> Identifier("minecraft", "block/stone")
            BaseStoneType.DEEPSLATE -> Identifier("minecraft", "block/deepslate")
        }
        OreModelCard.model.with(
            TextureKey.BACK to baseStoneTexture,
            TextureKey.FRONT to "block/" * MirageFairy2024.identifier(texturePath),
        )
    }
}

object OreModelCard {
    val parentModel = createOreModel()
    val identifier = MirageFairy2024.identifier("block/ore")
    val model = Model(identifier, TextureKey.BACK, TextureKey.FRONT)
}

context(ModContext)
fun initOresModule() {

    registerModelGeneration({ OreModelCard.identifier }) { OreModelCard.parentModel.with() }

    OreCard.entries.forEach { card ->

        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerSingletonBlockStateGeneration()
        card.block.registerModelGeneration(card.texturedModelFactory)
        card.block.registerCutoutRenderLayer()

        card.block.enJa(EnJa(card.enName, card.jaName))
        if (card.poemList != null) {
            card.item.registerPoem(card.poemList)
            card.item.registerPoemGeneration(card.poemList)
        }

        card.block.registerOreLootTableGeneration(card.dropItem)

        card.block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_PICKAXE }
        card.block.registerBlockTagGeneration { BlockTags.NEEDS_STONE_TOOL }
        card.block.registerBlockTagGeneration { ConventionalBlockTags.ORES }

    }

    fun worldGen(range: IntRange, countPerCube: Double, size: Int, card: OreCard) {

        val configuredKey = registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, card.identifier) {
            val targets = when (card.baseStoneType) {
                BaseStoneType.STONE -> listOf(OreFeatureConfig.createTarget(TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES), card.block.defaultBlockState()))
                BaseStoneType.DEEPSLATE -> listOf(OreFeatureConfig.createTarget(TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), card.block.defaultBlockState()))
            }
            Feature.ORE with OreFeatureConfig(targets, size)
        }

        registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, card.identifier) {
            val placementModifiers = placementModifiers { randomIntCount(countPerCube * (range.last - range.first + 1).toDouble() / 16.0) + uniformOre(range.first, range.last) }
            RegistryKeys.CONFIGURED_FEATURE[configuredKey] with placementModifiers
        }.also {
            it.registerFeature(GenerationStep.Feature.UNDERGROUND_ORES) { overworld }
        }

    }
    worldGen(16 until 128, 1.6, 12, OreCard.MAGNETITE_ORE)
    worldGen(16 until 128, 1.6, 12, OreCard.DEEPSLATE_MAGNETITE_ORE)
    worldGen(0 until 64, 1.2, 8, OreCard.FLUORITE_ORE)
    worldGen(0 until 64, 1.2, 8, OreCard.DEEPSLATE_FLUORITE_ORE)
    worldGen(-64 until 128, 0.6, 12, OreCard.MIRANAGITE_ORE)
    worldGen(-64 until 128, 0.6, 12, OreCard.DEEPSLATE_MIRANAGITE_ORE)

}

fun createOreModel() = Model {
    ModelData(
        parent = Identifier("minecraft", "block/block"),
        textures = ModelTexturesData(
            TextureKey.PARTICLE.id to TextureKey.BACK.string,
        ),
        elements = ModelElementsData(
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    down = ModelFaceData(texture = TextureKey.BACK.string, cullface = "down"),
                    up = ModelFaceData(texture = TextureKey.BACK.string, cullface = "up"),
                    north = ModelFaceData(texture = TextureKey.BACK.string, cullface = "north"),
                    south = ModelFaceData(texture = TextureKey.BACK.string, cullface = "south"),
                    west = ModelFaceData(texture = TextureKey.BACK.string, cullface = "west"),
                    east = ModelFaceData(texture = TextureKey.BACK.string, cullface = "east"),
                ),
            ),
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    down = ModelFaceData(texture = TextureKey.FRONT.string, cullface = "down"),
                    up = ModelFaceData(texture = TextureKey.FRONT.string, cullface = "up"),
                    north = ModelFaceData(texture = TextureKey.FRONT.string, cullface = "north"),
                    south = ModelFaceData(texture = TextureKey.FRONT.string, cullface = "south"),
                    west = ModelFaceData(texture = TextureKey.FRONT.string, cullface = "west"),
                    east = ModelFaceData(texture = TextureKey.FRONT.string, cullface = "east"),
                ),
            ),
        ),
    )
}
