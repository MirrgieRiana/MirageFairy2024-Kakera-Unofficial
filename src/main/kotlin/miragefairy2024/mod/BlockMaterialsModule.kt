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
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCompressionRecipeGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerTranslucentRenderLayer
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.HalfTransparentBlock as TransparentBlock
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.BlockGetter as BlockView
import net.minecraft.world.level.Level as World

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
    val texturedModelFactory: TexturedModel.Factory? = null,
    val isCutoutRenderLayer: Boolean = false,
    val isTranslucentRenderLayer: Boolean = false,
) {
    NEPHRITE_BLOCK(
        "nephrite_block", "Nephrite Block", "ネフライトブロック",
        PoemList(null),
        MapColor.BRIGHT_TEAL, 5.0F, 5.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL, BlockTags.BEACON_BASE_BLOCKS),
    ),
    XARPITE_BLOCK(
        "xarpite_block", "Xarpite Block", "紅天石ブロック",
        PoemList(2).poem("Loss and reconstruction of perception", "夢の世界の如き紅。"),
        MapColor.DARK_RED, 3.0F, 3.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL, BlockTags.BEACON_BASE_BLOCKS),
    ),
    MIRANAGITE_BLOCK(
        "miranagite_block", "Miranagite Block", "蒼天石ブロック",
        PoemList(2).poem("Passivation confines discontinuous space", "虚空に導かれし、霊界との接合点。"),
        MapColor.LAPIS_BLUE, 3.0F, 3.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL, BlockTags.BEACON_BASE_BLOCKS),
    ),
    CHAOS_STONE_BLOCK(
        "chaos_stone_block", "Chaos Stone Block", "混沌の石ブロック",
        PoemList(4).poem("The eye of entropy.", "無秩序の目。"),
        MapColor.TERRACOTTA_ORANGE, 5.0F, 5.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL, BlockTags.BEACON_BASE_BLOCKS),
    ),
    MIRAGIDIAN_BLOCK(
        "miragidian_block", "Miragidian Block", "ミラジディアンブロック",
        PoemList(4).poem("The wall feels like it's protecting us", "その身に宿る、黒曜石の魂。"),
        MapColor.TERRACOTTA_BLUE, 120.0F, 1200.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.BEACON_BASE_BLOCKS),
    ),
    LUMINITE_BLOCK(
        "luminite_block", "Luminite Block", "ルミナイトブロック",
        PoemList(4).poem("Catalytic digestion of astral vortices", "光り輝く魂のエネルギー。"),
        MapColor.DIAMOND_BLUE, 6.0F, 6.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL, BlockTags.BEACON_BASE_BLOCKS),
        isTranslucentRenderLayer = true, blockSoundGroup = BlockSoundGroup.GLASS,
        blockCreator = { SemiOpaqueTransparentBlock(it.nonOpaque().luminance { 15 }.solidBlock { _, _, _ -> false }) },
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
        texturedModelFactory = localVacuumDecayTexturedModelFactory, isCutoutRenderLayer = true, blockSoundGroup = BlockSoundGroup.SLIME,
    ),
    AURA_STONE(
        "aura_stone", "Aura Stone", "霊氣石",
        PoemList(3).poem("It absorbs auras and seals them away", "呼吸する石。"),
        MapColor.DIAMOND_BLUE, 5.0F, 6.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL),
        blockSoundGroup = BlockSoundGroup.METAL,
    ),
    ;

    val identifier = MirageFairy2024.identifier(path)
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

context(ModContext)
fun initBlockMaterialsModule() {
    BlockMaterialCard.entries.forEach { card ->
        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerSingletonBlockStateGeneration()
        if (card.texturedModelFactory != null) {
            card.block.registerModelGeneration(card.texturedModelFactory)
        } else {
            card.block.registerModelGeneration(TexturedModel.CUBE_ALL)
        }
        if (card.isCutoutRenderLayer) card.block.registerCutoutRenderLayer()
        if (card.isTranslucentRenderLayer) card.block.registerTranslucentRenderLayer()

        card.block.enJa(EnJa(card.enName, card.jaName))
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.block.registerDefaultLootTableGeneration()

        card.tags.forEach {
            card.block.registerBlockTagGeneration { it }
        }
    }

    // 圧縮
    registerCompressionRecipeGeneration(MaterialCard.XARPITE.item, BlockMaterialCard.XARPITE_BLOCK.item)
    registerCompressionRecipeGeneration(MaterialCard.MIRANAGITE.item, BlockMaterialCard.MIRANAGITE_BLOCK.item)
    registerCompressionRecipeGeneration(MaterialCard.CHAOS_STONE.item, BlockMaterialCard.CHAOS_STONE_BLOCK.item)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGIDIAN.item, BlockMaterialCard.MIRAGIDIAN_BLOCK.item)
    registerCompressionRecipeGeneration(MaterialCard.LUMINITE.item, BlockMaterialCard.LUMINITE_BLOCK.item)

    // 霊氣石
    registerShapedRecipeGeneration(BlockMaterialCard.AURA_STONE.item) {
        pattern("XMX")
        pattern("MCM")
        pattern("XMX")
        input('X', MaterialCard.XARPITE.item)
        input('M', MaterialCard.MIRANAGITE.item)
        input('C', MaterialCard.FAIRY_CRYSTAL.item)
    } on MaterialCard.FAIRY_CRYSTAL.item

}

private val localVacuumDecayTexturedModelFactory = TexturedModel.Factory { block ->
    Model { textureMap ->
        ModelData(
            parent = Identifier("minecraft", "block/block"),
            textures = ModelTexturesData(
                TextureKey.PARTICLE.name to textureMap.getTexture(TextureKey.BACK).string,
                TextureKey.BACK.name to textureMap.getTexture(TextureKey.BACK).string,
                TextureKey.FRONT.name to textureMap.getTexture(TextureKey.FRONT).string,
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
    }.with(
        TextureKey.BACK to "block/" * block.getIdentifier() * "_base",
        TextureKey.FRONT to "block/" * block.getIdentifier() * "_spark",
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

class SemiOpaqueTransparentBlock(settings: Settings) : TransparentBlock(settings) {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos) = 1
}
