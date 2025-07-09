package miragefairy2024.mod.materials.block

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipeCard
import miragefairy2024.mod.machine.registerSimpleMachineRecipeGeneration
import miragefairy2024.mod.materials.block.cards.FairyCrystalGlassBlock
import miragefairy2024.mod.materials.block.cards.LocalVacuumDecayBlock
import miragefairy2024.mod.materials.block.cards.SemiOpaqueTransparentBlock
import miragefairy2024.mod.materials.block.cards.fairyCrystalGlassBlockModel
import miragefairy2024.mod.materials.block.cards.fairyCrystalGlassFrameBlockModel
import miragefairy2024.mod.materials.block.cards.localVacuumDecayTexturedModelFactory
import miragefairy2024.mod.materials.item.MaterialCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockStateGeneration
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCompressionRecipeGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerTranslucentRenderLayer
import miragefairy2024.util.times
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock

open class BlockMaterialCard(
    path: String,
    private val name: EnJa,
    private val poemList: PoemList,
    private val mapColor: MapColor,
    private val hardness: Float,
    private val resistance: Float,
) {
    companion object {
        val entries = mutableListOf<BlockMaterialCard>()
        private operator fun BlockMaterialCard.not() = this.apply { entries.add(this) }

        val NEPHRITE_BLOCK = !BlockMaterialCard(
            "nephrite_block", EnJa("Nephrite Block", "ネフライトブロック"),
            PoemList(null),
            MapColor.WARPED_WART_BLOCK, 5.0F, 5.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase()
        val XARPITE_BLOCK = !BlockMaterialCard(
            "xarpite_block", EnJa("Xarpite Block", "紅天石ブロック"),
            PoemList(2).poem(EnJa("Loss and reconstruction of perception", "夢の世界の如き紅。")),
            MapColor.NETHER, 3.0F, 3.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.XARPITE.item, item)
        }
        val MIRANAGITE_BLOCK = !BlockMaterialCard(
            "miranagite_block", EnJa("Miranagite Block", "蒼天石ブロック"),
            PoemList(2).poem(EnJa("Passivation confines discontinuous space", "虚空に導かれし、神域との接合点。")),
            MapColor.LAPIS, 3.0F, 3.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.MIRANAGITE.item, item)
        }
        val CHAOS_STONE_BLOCK = !BlockMaterialCard(
            "chaos_stone_block", EnJa("Chaos Stone Block", "混沌の石ブロック"),
            PoemList(4).poem(EnJa("The eye of entropy.", "無秩序の目。")),
            MapColor.TERRACOTTA_ORANGE, 5.0F, 5.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.STONE).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.CHAOS_STONE.item, item)
        }
        val MIRAGIDIAN_BLOCK = !BlockMaterialCard(
            "miragidian_block", EnJa("Miragidian Block", "ミラジディアンブロック"),
            PoemList(4).poem(EnJa("The wall feels like it's protecting us", "その身に宿る、黒曜石の魂。")),
            MapColor.TERRACOTTA_BLUE, 120.0F, 1200.0F,
        ).needTool(ToolType.PICKAXE, ToolLevel.DIAMOND).noBurn().beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.MIRAGIDIAN.item, item)
        }
        val LUMINITE_BLOCK = !object : BlockMaterialCard(
            "luminite_block", EnJa("Luminite Block", "ルミナイトブロック"),
            PoemList(4).poem(EnJa("Catalytic digestion of astral vortices", "光り輝く魂のエネルギー。")),
            MapColor.DIAMOND, 6.0F, 6.0F,
        ) {
            override fun createBlockProperties(): AbstractBlock.Properties = super.createBlockProperties()
                .noOcclusion()
                .lightLevel { 15 }
                .isRedstoneConductor { _, _, _ -> false }

            override suspend fun createBlock(properties: AbstractBlock.Properties) = SemiOpaqueTransparentBlock(properties)
        }.translucent().sound(SoundType.GLASS).needTool(ToolType.PICKAXE, ToolLevel.IRON).beaconBase().init {
            registerCompressionRecipeGeneration(MaterialCard.LUMINITE.item, item)
        }
        val DRYWALL = !BlockMaterialCard(
            "drywall", EnJa("Drywall", "石膏ボード"),
            PoemList(1).poem(EnJa("Please use on the office ceiling, etc.", "オフィスの天井等にどうぞ。")),
            MapColor.SAND, 3.0F, 3.0F,
        ).tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE)
        val LOCAL_VACUUM_DECAY = !object : BlockMaterialCard(
            "local_vacuum_decay", EnJa("Local Vacuum Decay", "局所真空崩壊"),
            PoemList(99).poem(EnJa("Stable instability due to anti-entropy", "これが秩序の究極の形だというのか？")),
            MapColor.COLOR_BLACK, -1.0F, 3600000.0F,
        ) {
            override suspend fun createBlock(properties: AbstractBlock.Properties) = LocalVacuumDecayBlock(properties)
            context(ModContext) override fun initModelGeneration() = block.registerModelGeneration(localVacuumDecayTexturedModelFactory)
        }.cutout().sound(SoundType.SLIME_BLOCK).invincible().speed(0.5F)
        val AURA_STONE = !BlockMaterialCard(
            "aura_stone", EnJa("Aura Stone", "霊氣石"),
            PoemList(3).poem(EnJa("It absorbs auras and seals them away", "呼吸する石。")),
            MapColor.DIAMOND, 5.0F, 6.0F,
        ).sound(SoundType.METAL).needTool(ToolType.PICKAXE, ToolLevel.IRON).init {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(MaterialCard.FAIRY_CRYSTAL.item()) }, 1),
                    Pair({ Ingredient.of(MaterialCard.XARPITE.item()) }, 4),
                    Pair({ Ingredient.of(MaterialCard.MIRANAGITE.item()) }, 4),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60,
            ) on MaterialCard.FAIRY_CRYSTAL.item
        }
        val FAIRY_CRYSTAL_GLASS = !object : BlockMaterialCard(
            "fairy_crystal_glass", EnJa("Fairy Crystal Glass", "フェアリークリスタルガラス"),
            PoemList(2).poem(EnJa("It is displaying the scene behind it.", "家の外を映し出す鏡。")),
            MapColor.DIAMOND, 1.5F, 1.5F,
        ) {
            override fun createBlockProperties(): AbstractBlock.Properties = super.createBlockProperties()
                .instrument(NoteBlockInstrument.HAT)
                .noOcclusion()
                .isRedstoneConductor(Blocks::never)
                .isSuffocating(Blocks::never)
                .isViewBlocking(Blocks::never)

            override suspend fun createBlock(properties: AbstractBlock.Properties) = FairyCrystalGlassBlock(properties)

            context(ModContext)
            override fun initBlockStateGeneration() {
                block.registerBlockStateGeneration {
                    fun createPart(direction: String, x: Int, y: Int) = jsonObject(
                        "when" to jsonObject(
                            direction to "false".jsonElement,
                        ),
                        "apply" to jsonObject(
                            "model" to "${"block/" * identifier * "_frame"}".jsonElement,
                            "x" to x.jsonElement,
                            "y" to y.jsonElement,
                        ),
                    )
                    jsonObject(
                        "multipart" to jsonArray(
                            createPart("north", 90, 0),
                            createPart("east", 90, 90),
                            createPart("south", -90, 0),
                            createPart("west", 90, -90),
                            createPart("up", 0, 0),
                            createPart("down", 180, 0),
                        ),
                    )
                }
            }

            context(ModContext) override fun initModelGeneration() = Unit
        }.cutout().sound(SoundType.GLASS).needTool(ToolType.PICKAXE, ToolLevel.STONE).noSpawn().tag(BlockTags.IMPERMEABLE).init {
            // インベントリ内のモデル
            registerModelGeneration({ "block/" * identifier }) {
                fairyCrystalGlassBlockModel.with(TextureKey.TEXTURE to "block/" * identifier * "_frame")
            }
            // 枠パーツモデル
            registerModelGeneration({ "block/" * identifier * "_frame" }) {
                fairyCrystalGlassFrameBlockModel.with(TextureKey.TEXTURE to "block/" * identifier * "_frame")
            }

            registerCompressionRecipeGeneration(MaterialCard.FAIRY_CRYSTAL.item, item)
        }
    }

    val identifier = MirageFairy2024.identifier(path)
    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        val properties = blockPropertiesConverters.fold(createBlockProperties()) { properties, converter -> converter(properties) }
        createBlock(properties)
    }
    val item = Registration(BuiltInRegistries.ITEM, identifier) {
        val properties = itemPropertiesConverters.fold(Item.Properties()) { properties, converter -> converter(properties) }
        BlockItem(block.await(), properties)
    }

    val itemPropertiesConverters = mutableListOf<(Item.Properties) -> Item.Properties>()
    val blockPropertiesConverters = mutableListOf<(AbstractBlock.Properties) -> AbstractBlock.Properties>()
    val initializers = mutableListOf<(ModContext) -> Unit>()

    open fun createBlockProperties(): AbstractBlock.Properties = AbstractBlock.Properties.of()
        .mapColor(mapColor)
        .strength(hardness, resistance)

    open suspend fun createBlock(properties: AbstractBlock.Properties) = Block(properties)

    context(ModContext)
    open fun init() {

        block.register()
        item.register()

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        initBlockStateGeneration()
        initModelGeneration()

        block.enJa(name)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerDefaultLootTableGeneration()

        initializers.forEach {
            it(this@ModContext)
        }

    }

    context(ModContext)
    open fun initBlockStateGeneration() {
        block.registerSingletonBlockStateGeneration()
    }

    context(ModContext)
    open fun initModelGeneration() {
        block.registerModelGeneration(TexturedModel.CUBE)
    }
}

context(ModContext)
fun initBlockMaterialsModule() {
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("local_vacuum_decay")) { LocalVacuumDecayBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("semi_opaque_transparent_block")) { SemiOpaqueTransparentBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_crystal_glass")) { FairyCrystalGlassBlock.CODEC }.register()

    BlockMaterialCard.entries.forEach { card ->
        card.init()
    }
}


private fun <T : BlockMaterialCard> T.blockProperty(converter: (AbstractBlock.Properties) -> AbstractBlock.Properties) = this.also { it.blockPropertiesConverters += converter }
private fun <T : BlockMaterialCard> T.itemProperty(converter: (Item.Properties) -> Item.Properties) = this.also { it.itemPropertiesConverters += converter }

private fun <T : BlockMaterialCard> T.noDrop() = this.blockProperty { it.noLootTable() }
private fun <T : BlockMaterialCard> T.noSpawn() = this.blockProperty { it.isValidSpawn(Blocks::never) }
private fun <T : BlockMaterialCard> T.speed(speedFactor: Float) = this.blockProperty { it.speedFactor(speedFactor) }
private fun <T : BlockMaterialCard> T.sound(blockSoundGroup: SoundType) = this.blockProperty { it.sound(blockSoundGroup) }

private fun <T : BlockMaterialCard> T.noBurn() = this.itemProperty { it.fireResistant() }

private fun <T : BlockMaterialCard> T.init(initializer: context(ModContext) T.() -> Unit) = this.also {
    this.initializers += { modContext ->
        initializer(modContext, this)
    }
}

private fun <T : BlockMaterialCard> T.cutout() = this.init {
    block.registerCutoutRenderLayer()
}

private fun <T : BlockMaterialCard> T.translucent() = this.init {
    block.registerTranslucentRenderLayer()
}

private fun <T : BlockMaterialCard> T.tag(vararg tags: TagKey<Block>) = this.init {
    tags.forEach {
        block.registerBlockTagGeneration { it }
    }
}

enum class ToolType(val tag: TagKey<Block>) {
    AXE(BlockTags.MINEABLE_WITH_AXE),
    HOE(BlockTags.MINEABLE_WITH_HOE),
    PICKAXE(BlockTags.MINEABLE_WITH_PICKAXE),
    SHOVEL(BlockTags.MINEABLE_WITH_SHOVEL),
}

enum class ToolLevel(val tag: TagKey<Block>) {
    DIAMOND(BlockTags.NEEDS_DIAMOND_TOOL),
    IRON(BlockTags.NEEDS_IRON_TOOL),
    STONE(BlockTags.NEEDS_STONE_TOOL),
}

private fun <T : BlockMaterialCard> T.needTool(type: ToolType, level: ToolLevel) = this.blockProperty { it.requiresCorrectToolForDrops() }.tag(type.tag, level.tag)

private fun <T : BlockMaterialCard> T.beaconBase() = this.tag(BlockTags.BEACON_BASE_BLOCKS)

private fun <T : BlockMaterialCard> T.invincible() = this
    .noDrop()
    .noSpawn()
    .tag(
        BlockTags.DRAGON_IMMUNE,
        BlockTags.WITHER_IMMUNE,
        BlockTags.FEATURES_CANNOT_REPLACE,
        BlockTags.GEODE_INVALID_BLOCKS,
        BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS,
    )
