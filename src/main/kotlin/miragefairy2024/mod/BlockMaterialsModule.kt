package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerTagGeneration
import miragefairy2024.util.string
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.data.client.TextureKey
import net.minecraft.data.client.TextureMap
import net.minecraft.data.client.TexturedModel
import net.minecraft.entity.Entity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

enum class BlockMaterialCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList,
    mapColor: MapColor,
    hardness: Float,
    resistance: Float,
    requiresTool: Boolean = false,
    dropsNothing: Boolean = false,
    restrictsSpawning: Boolean = false,
    velocityMultiplier: Float? = null,
    blockSoundGroup: BlockSoundGroup? = null,
    blockCreator: ((AbstractBlock.Settings) -> Block)? = null,
    val tags: List<TagKey<Block>> = listOf(),
    val model: TexturedModel.Factory? = null,
    val isCutoutRenderLayer: Boolean = false,
) {
    MIRANAGITE_BLOCK(
        "miranagite_block", "Miranagite Block", "蒼天石ブロック",
        PoemList(2).poem("Passivation confines discontinuous space", "虚空に導かれし、霊界との接合点。"),
        MapColor.LAPIS_BLUE, 3.0F, 3.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL),
    ),
    DRYWALL(
        "drywall", "Drywall", "石膏ボード",
        PoemList(1).poem("Please use on the office ceiling, etc.", "オフィスの天井等にどうぞ。"),
        MapColor.PALE_YELLOW, 3.0F, 3.0F,
        tags = listOf(BlockTags.PICKAXE_MINEABLE),
    ),
    LOCAL_VACUUM_DECAY(
        "local_vacuum_decay", "Local Vacuum Decay", "局所真空崩壊",
        PoemList(99).poem("Stable instability due to anti-entropy", "これが秩序の究極の形だというのか？"),
        MapColor.BLACK, -1.0F, 3600000.0F, dropsNothing = true, restrictsSpawning = true, blockCreator = ::LocalVacuumDecayBlock, velocityMultiplier = 0.5F,
        tags = listOf(BlockTags.DRAGON_IMMUNE, BlockTags.WITHER_IMMUNE, BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS),
        model = localVacuumDecayTexturedModel, isCutoutRenderLayer = true, blockSoundGroup = BlockSoundGroup.SLIME,
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val block = run {
        val settings = AbstractBlock.Settings.create()
        settings.mapColor(mapColor)
        if (requiresTool) settings.requiresTool()
        if (dropsNothing) settings.dropsNothing()
        if (restrictsSpawning) settings.allowsSpawning { _, _, _, _ -> false }
        if (velocityMultiplier != null) settings.velocityMultiplier(velocityMultiplier)
        settings.strength(hardness, resistance)
        if (blockSoundGroup != null) settings.sounds(blockSoundGroup)
        if (blockCreator != null) blockCreator(settings) else Block(settings)
    }
    val item = BlockItem(block, Item.Settings())
}

fun initBlockMaterialsModule() {
    BlockMaterialCard.entries.forEach { card ->
        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroup)

        card.block.registerSingletonBlockStateGeneration()
        if (card.model != null) {
            card.block.registerModelGeneration(card.model)
        } else {
            card.block.registerModelGeneration(TexturedModel.CUBE_ALL)
        }
        if (card.isCutoutRenderLayer) card.block.registerCutoutRenderLayer()

        card.block.enJa(card.enName, card.jaName)
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.block.registerDefaultLootTableGeneration()

        card.tags.forEach {
            card.block.registerTagGeneration(it)
        }
    }

    registerShapedRecipeGeneration(BlockMaterialCard.MIRANAGITE_BLOCK.item) {
        pattern("###")
        pattern("###")
        pattern("###")
        input('#', MaterialCard.MIRANAGITE.item)
    } on MaterialCard.MIRANAGITE.item from MaterialCard.MIRANAGITE.item
    registerShapelessRecipeGeneration(MaterialCard.MIRANAGITE.item, 9) {
        input(BlockMaterialCard.MIRANAGITE_BLOCK.item)
    } on BlockMaterialCard.MIRANAGITE_BLOCK.item from BlockMaterialCard.MIRANAGITE_BLOCK.item

}

private val localVacuumDecayTexturedModel = TexturedModel.Factory { block ->
    Model { textures ->
        ModelData(
            parent = Identifier("minecraft", "block/block"),
            textures = ModelTexturesData(
                TextureKey.PARTICLE.name to textures.getTexture(TextureKey.BACK).string,
                TextureKey.BACK.name to textures.getTexture(TextureKey.BACK).string,
                TextureKey.FRONT.name to textures.getTexture(TextureKey.FRONT).string,
            ),
            elements = ModelElementsData(
                ModelElementData(
                    from = listOf(0, 0, 0),
                    to = listOf(16, 16, 16),
                    faces = jsonObject(
                        "down" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "down".jsonElement),
                        "up" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "up".jsonElement),
                        "north" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "north".jsonElement),
                        "south" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "south".jsonElement),
                        "west" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "west".jsonElement),
                        "east" to jsonObject("texture" to TextureKey.BACK.string.jsonElement, "cullface" to "east".jsonElement),
                    ),
                ),
                ModelElementData(
                    from = listOf(0, 0, 0),
                    to = listOf(16, 16, 16),
                    faces = jsonObject(
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
    }.with(
        TextureKey.BACK to TextureMap.getSubId(block, "_base"),
        TextureKey.FRONT to TextureMap.getSubId(block, "_spark"),
    )
}

@Suppress("OVERRIDE_DEPRECATION")
class LocalVacuumDecayBlock(settings: Settings) : Block(settings) {
    override fun hasRandomTicks(state: BlockState) = true

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        @Suppress("DEPRECATION")
        super.randomTick(state, world, pos, random)

        val direction = Direction.random(random)
        val targetBlockPos = pos.offset(direction)
        val targetBlockState = world.getBlockState(targetBlockPos)
        if (targetBlockState.isAir) return
        if (targetBlockState.getHardness(world, targetBlockPos) < 0) return
        if (targetBlockState.isOf(state.block)) return
        world.setBlockState(targetBlockPos, state)
    }

    override fun onSteppedOn(world: World, pos: BlockPos, state: BlockState, entity: Entity) {
        if (!entity.bypassesSteppingEffects()) {
            entity.damage(world.damageSources.magic(), 1.0f)
        }
        super.onSteppedOn(world, pos, state, entity)
    }
}
