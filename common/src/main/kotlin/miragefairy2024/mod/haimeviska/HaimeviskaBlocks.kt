package miragefairy2024.mod.haimeviska

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.materials.item.MaterialCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Registration
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.get
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.string
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.BlockSetTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.WoodTypeBuilder
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.BlockFamily
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.FenceBlock
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.material.MapColor
import java.util.Optional
import net.minecraft.world.level.block.RotatedPillarBlock as PillarBlock
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument as Instrument
import net.minecraft.world.level.material.PushReaction as PistonBehavior
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount as ApplyBonusLootFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition as RandomChanceLootCondition

class HaimeviskaBlockConfiguration(
    val path: String,
    val name: EnJa,
    val poemList: PoemList,
)

open class HaimeviskaBlockCard(
    val configuration: HaimeviskaBlockConfiguration,
    blockCreator: suspend () -> Block,
    val initializer: context(ModContext)(HaimeviskaBlockCard) -> Unit,
    val extraInitializer: context(ModContext) HaimeviskaBlockCard.() -> Unit,
) {
    companion object {
        val entries = mutableListOf<HaimeviskaBlockCard>()
        private operator fun HaimeviskaBlockCard.not() = apply { entries += this }

        val LEAVES = !HaimeviskaBlockConfiguration(
            "haimeviska_leaves", EnJa("Haimeviska Leaves", "ハイメヴィスカの葉"),
            PoemList(1).poem(EnJa("All original flowers are consumed by ivy", "妖精になれる花、なれない花。")),
        ).let { HaimeviskaLeavesBlockCard(it) }
        val LOG = !HaimeviskaBlockConfiguration(
            "haimeviska_log", EnJa("Haimeviska Log", "ハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Symbiosis with parasitic Mirages", "妖精の滲み込んだ樹。"))
                .description(EnJa("Can be incised with a sword", "剣を使って傷を付けられる")),
        ).let { HaimeviskaLogBlockCard(it) }
        val WOOD = !HaimeviskaBlockConfiguration(
            "haimeviska_wood", EnJa("Haimeviska Wood", "ハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Hydraulic communication system", "ウィスプたちの集合知。")),
        ).let { HaimeviskaWoodBlockCard(it) }
        val STRIPPED_LOG = !HaimeviskaBlockConfiguration(
            "stripped_haimeviska_log", EnJa("Stripped Haimeviska Log", "樹皮を剥いだハイメヴィスカの原木"),
            PoemList(1).poem(EnJa("Something lacking the essence", "ぬぐわれたペルソナ。")),
        ).let { HaimeviskaStrippedLogBlockCard(it) }
        val STRIPPED_WOOD = !HaimeviskaBlockConfiguration(
            "stripped_haimeviska_wood", EnJa("Stripped Haimeviska Wood", "樹皮を剥いだハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Loss of self", "寄生蔦からの解放。")),
        ).let { HaimeviskaStrippedWoodBlockCard(it) }
        val INCISED_LOG = !HaimeviskaBlockConfiguration(
            "incised_haimeviska_log", EnJa("Incised Haimeviska Log", "傷の付いたハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Do fairy trees have qualia of pain?", "動物を守るということ。"))
                .description(EnJa("Produces sap over time", "時間経過で樹液を生産")),
        ).let { HaimeviskaIncisedLogBlockCard(it) }
        val DRIPPING_LOG = !HaimeviskaBlockConfiguration(
            "dripping_haimeviska_log", EnJa("Dripping Haimeviska Log", "滴るハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("A spirit named 'glucose'", "霊界より降りしもの。"))
                .description(EnJa("Harvest sap when used", "使用時、樹液を収穫")),
        ).let { HaimeviskaDrippingLogBlockCard(it) }
        val HOLLOW_LOG = !HaimeviskaBlockConfiguration(
            "hollow_haimeviska_log", EnJa("Hollow Haimeviska Log", "ハイメヴィスカの樹洞"),
            PoemList(1).poem(EnJa("Auric conceptual attractor", "限界巡回アステリア。")),
        ).let { HaimeviskaHollowLogBlockCard(it) }
        val PLANKS = !HaimeviskaBlockConfiguration(
            "haimeviska_planks", EnJa("Haimeviska Planks", "ハイメヴィスカの板材"),
            PoemList(1).poem(EnJa("Flexible and friendly, good for interior", "考える、壁。")),
        ).let { HaimeviskaPlanksBlockCard(it) }
        val SLAB = !HaimeviskaBlockConfiguration(
            "haimeviska_slab", EnJa("Haimeviska Slab", "ハイメヴィスカのハーフブロック"),
            PoemList(1).poem(EnJa("Searching for another personality.", "半人前の側頭葉。")),
        ).let { HaimeviskaSlabBlockCard(it) }
        val STAIRS = !HaimeviskaBlockConfiguration(
            "haimeviska_stairs", EnJa("Haimeviska Stairs", "ハイメヴィスカの階段"),
            PoemList(1).poem(EnJa("Step that pierces the sky", "情緒体を喰らう頂となれ。")),
        ).let { HaimeviskaStairsBlockCard(it) }
        val FENCE = !HaimeviskaBlockConfiguration(
            "haimeviska_fence", EnJa("Haimeviska Fence", "ハイメヴィスカのフェンス"),
            PoemList(1).poem(EnJa("Personality flowing through the xylem", "樹のなかに住む。")),
        ).let { HaimeviskaFenceBlockCard(it) }
        val FENCE_GATE = !HaimeviskaBlockConfiguration(
            "haimeviska_fence_gate", EnJa("Haimeviska Fence Gate", "ハイメヴィスカのフェンスゲート"),
            PoemList(1).poem(EnJa("It chose this path of its own will", "知性の邂逅。")),
        ).let { HaimeviskaFenceGateBlockCard(it) }
        val BUTTON = !HaimeviskaBlockConfiguration(
            "haimeviska_button", EnJa("Haimeviska Button", "ハイメヴィスカのボタン"),
            PoemList(1).poem(EnJa("What is this soft and warm thing?", "指先の感触。")),
        ).let { HaimeviskaButtonBlockCard(it) }
        val PRESSURE_PLATE = !HaimeviskaBlockConfiguration(
            "haimeviska_pressure_plate", EnJa("Haimeviska Pressure Plate", "ハイメヴィスカの感圧板"),
            PoemList(1).poem(EnJa("Creature with the name of a machine", "感応と感覚の違い。")),
        ).let { HaimeviskaPressurePlateBlockCard(it) }
        val BRICKS = !HaimeviskaBlockConfiguration(
            "haimeviska_bricks", EnJa("Haimeviska Bricks", "ハイメヴィスカレンガ"),
            PoemList(1).poem(EnJa("An ecosystem called 'civilization'", "人がもたらした原生林。")),
        ).let { HaimeviskaBricksBlockCard(it) }
        val BRICKS_SLAB = !HaimeviskaBlockConfiguration(
            "haimeviska_bricks_slab", EnJa("Haimeviska Bricks Slab", "ハイメヴィスカレンガのハーフブロック"),
            PoemList(1).poem(EnJa("Extremely modularized memory", "ひとまわり細かくなった私。")),
        ).let { HaimeviskaBricksSlabBlockCard(it) }
        val BRICKS_STAIRS = !HaimeviskaBlockConfiguration(
            "haimeviska_bricks_stairs", EnJa("Haimeviska Bricks Stairs", "ハイメヴィスカレンガの階段"),
            PoemList(1).poem(EnJa("Forgotten paths of the technology", "生体工学の歩み。")),
        ).let { HaimeviskaBricksStairsBlockCard(it) }
        val SAPLING = !HaimeviskaBlockConfiguration(
            "haimeviska_sapling", EnJa("Haimeviska Sapling", "ハイメヴィスカの苗木"),
            PoemList(1).poem(EnJa("Assembling molecules with Ergs", "第二の葉緑体。")),
        ).let { HaimeviskaSaplingBlockCard(it) }
    }

    val identifier = MirageFairy2024.identifier(configuration.path)
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { blockCreator() }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }

    context(ModContext)
    fun init() {

        // 登録
        block.register()
        item.register()

        // カテゴリ
        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // テキスト
        block.enJa(configuration.name)
        item.registerPoem(configuration.poemList)
        item.registerPoemGeneration(configuration.poemList)

        initializer(this@ModContext, this)
        extraInitializer(this@ModContext, this)

    }
}

fun createBaseWoodSetting(sound: Boolean = true) = AbstractBlock.Properties.of().instrument(Instrument.BASS).let { if (sound) it.sound(BlockSoundGroup.WOOD) else it }.ignitedByLava()
fun createSpecialLogSettings() = createBaseWoodSetting().strength(2.0F).mapColor(MapColor.RAW_IRON)

fun createTreeGrower(identifier: ResourceLocation) = TreeGrower(identifier.string, Optional.empty(), Optional.of(HAIMEVISKA_CONFIGURED_FEATURE_KEY), Optional.empty())


lateinit var HAIMEVISKA_BLOCK_SET_TYPE: BlockSetType
lateinit var HAIMEVISKA_WOOD_TYPE: WoodType

val HAIMEVISKA_LOGS_BLOCK_TAG: TagKey<Block> = TagKey.create(Registries.BLOCK, MirageFairy2024.identifier("haimeviska_logs"))
val HAIMEVISKA_LOGS_ITEM_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, MirageFairy2024.identifier("haimeviska_logs"))

private val familyRegistry = mutableListOf<Pair<() -> Block, (BlockFamily.Builder) -> BlockFamily.Builder>>()

context(ModContext)
fun initHaimeviskaBlocks() {

    HaimeviskaBlockCard.entries.forEach { card ->
        card.init()
    }

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_leaves")) { HaimeviskaLeavesBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_log")) { HaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_haimeviska_log")) { IncisedHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("dripping_haimeviska_log")) { DrippingHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("hollow_haimeviska_log")) { HollowHaimeviskaLogBlock.CODEC }.register()

    // Wood Type
    HAIMEVISKA_BLOCK_SET_TYPE = BlockSetTypeBuilder().register(MirageFairy2024.identifier("haimeviska"))
    HAIMEVISKA_WOOD_TYPE = WoodTypeBuilder().register(MirageFairy2024.identifier("haimeviska"), HAIMEVISKA_BLOCK_SET_TYPE)

    // タグ
    HAIMEVISKA_LOGS_BLOCK_TAG.registerBlockTagGeneration { BlockTags.LOGS_THAT_BURN }
    HAIMEVISKA_LOGS_ITEM_TAG.registerItemTagGeneration { ItemTags.LOGS_THAT_BURN }

    val families by lazy {
        familyRegistry
            .map { Pair(it.first(), it.second) }
            .groupBy { it.first }
            .map { it.value.fold(BlockFamily.Builder(it.key)) { a, b -> b.second(a) }.family }
    }
    DataGenerationEvents.onGenerateBlockModel {
        families.forEach { family ->
            it.family(family.baseBlock).generateFor(family)
        }
    }
    DataGenerationEvents.onGenerateRecipe {
        families.forEach { family ->
            RecipeProvider.generateRecipes(it, family, FeatureFlagSet.of(FeatureFlags.VANILLA))
        }
    }

}

context(ModContext)
private fun registerBlockFamily(baseBlock: () -> Block, initializer: (BlockFamily.Builder) -> BlockFamily.Builder) {
    familyRegistry += Pair(baseBlock, initializer)
}


class HaimeviskaLeavesBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { HaimeviskaLeavesBlock(createLeavesSettings()) },
    ::initLeavesHaimeviskaBlock,
    {
        block.registerLootTableGeneration { it, _ ->
            it.createLeavesDrops(block(), SAPLING.block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F)
        }
        item.registerComposterInput(0.3F)
    },
)

class HaimeviskaLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { HaimeviskaLogBlock(createLogSettings()) },
    initLogHaimeviskaBlock(null),
    {
        block.registerDefaultLootTableGeneration()
    },
)

class HaimeviskaWoodBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { PillarBlock(createLogSettings(wood = true)) },
    initLogHaimeviskaBlock({ LOG }, wood = true),
    {
        block.registerDefaultLootTableGeneration()
        registerShapedRecipeGeneration(item, 3) {
            pattern("##")
            pattern("##")
            define('#', LOG.item())
        } on LOG.item
    },
)

class HaimeviskaStrippedLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { PillarBlock(createLogSettings(stripped = true)) },
    initLogHaimeviskaBlock(null, stripped = true),
    {
        block.registerDefaultLootTableGeneration()
        ModEvents.onInitialize {
            StrippableBlockRegistry.register(LOG.block(), block())
        }
    },
)

class HaimeviskaStrippedWoodBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { PillarBlock(createLogSettings(stripped = true, wood = true)) },
    initLogHaimeviskaBlock({ STRIPPED_LOG }, stripped = true, wood = true),
    {
        block.registerDefaultLootTableGeneration()
        registerShapedRecipeGeneration(item, 3) {
            pattern("##")
            pattern("##")
            define('#', STRIPPED_LOG.item())
        } on STRIPPED_LOG.item
        ModEvents.onInitialize {
            StrippableBlockRegistry.register(WOOD.block(), block())
        }
    },
)

class HaimeviskaIncisedLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { IncisedHaimeviskaLogBlock(createSpecialLogSettings()) },
    ::initHorizontalFacingLogHaimeviskaBlock,
    {
        block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
    },
)

class HaimeviskaDrippingLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { DrippingHaimeviskaLogBlock(createSpecialLogSettings()) },
    ::initHorizontalFacingLogHaimeviskaBlock,
    {
        block.registerLootTableGeneration { provider, registries ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_SAP.item()) {
                    apply(ApplyBonusLootFunction.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_ROSIN.item()) {
                    apply(ApplyBonusLootFunction.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE], 2))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                    `when`(RandomChanceLootCondition.randomChance(0.01F))
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
        item.registerHarvestNotation(MaterialCard.HAIMEVISKA_SAP.item, MaterialCard.HAIMEVISKA_ROSIN.item)
    },
)

class HaimeviskaHollowLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { HollowHaimeviskaLogBlock(createSpecialLogSettings()) },
    ::initHorizontalFacingLogHaimeviskaBlock,
    {
        block.registerLootTableGeneration { provider, registries ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
                LootPool(ItemLootPoolEntry(MaterialCard.FRACTAL_WISP.item()) {
                    apply(ApplyBonusLootFunction.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
                }) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }
        item.registerHarvestNotation(MaterialCard.FRACTAL_WISP.item)
    },
)

class HaimeviskaPlanksBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { Block(createPlankSettings()) },
    ::initPlanksHaimeviskaBlock,
    {
        block.registerDefaultLootTableGeneration()
        registerShapelessRecipeGeneration(item, 4) {
            requires(LOG.item())
        } on LOG.item from LOG.item
    },
)

class HaimeviskaSlabBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { SlabBlock(createPlankSettings()) },
    ::initPlanksSlabHaimeviskaBlock,
    {
        registerBlockFamily(PLANKS.block) { it.slab(block()) }
        block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }
    },
)

class HaimeviskaStairsBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { StairBlock(PLANKS.block.await().defaultBlockState(), createPlankSettings()) },
    ::initPlanksStairsHaimeviskaBlock,
    {
        registerBlockFamily(PLANKS.block) { it.stairs(block()) }
        block.registerDefaultLootTableGeneration()
    },
)

class HaimeviskaFenceBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { FenceBlock(createPlankSettings()) },
    ::initPlanksFenceHaimeviskaBlock,
    {
        registerBlockFamily(PLANKS.block) { it.fence(block()) }
        block.registerDefaultLootTableGeneration()
    },
)

class HaimeviskaFenceGateBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { FenceGateBlock(HAIMEVISKA_WOOD_TYPE, createPlankSettings(sound = false).forceSolidOn()) },
    ::initPlanksFenceGateHaimeviskaBlock,
    {
        registerBlockFamily(PLANKS.block) { it.fenceGate(block()) }
        block.registerDefaultLootTableGeneration()
    },
)

class HaimeviskaButtonBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { ButtonBlock(HAIMEVISKA_BLOCK_SET_TYPE, 30, AbstractBlock.Properties.of().noCollission().strength(0.5F).pushReaction(PistonBehavior.DESTROY)) },
    ::initPlanksButtonHaimeviskaBlock,
    {
        registerBlockFamily(PLANKS.block) { it.button(block()) }
        block.registerDefaultLootTableGeneration()
    },
)

class HaimeviskaPressurePlateBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { PressurePlateBlock(HAIMEVISKA_BLOCK_SET_TYPE, createBaseWoodSetting(sound = false).forceSolidOn().noCollission().strength(0.5F).pushReaction(PistonBehavior.DESTROY)) },
    ::initPlanksPressurePlateHaimeviskaBlock,
    {
        registerBlockFamily(PLANKS.block) { it.pressurePlate(block()) }
        block.registerDefaultLootTableGeneration()
    },
)

class HaimeviskaBricksBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { Block(createPlankSettings()) },
    ::initPlanksHaimeviskaBlock,
    {
        block.registerDefaultLootTableGeneration()
        registerShapedRecipeGeneration(item, 4) {
            pattern("##")
            pattern("##")
            define('#', PLANKS.item())
        } on PLANKS.item from PLANKS.item
    },
)

class HaimeviskaBricksSlabBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { SlabBlock(createPlankSettings()) },
    ::initPlanksSlabHaimeviskaBlock,
    {
        registerBlockFamily(BRICKS.block) { it.slab(block()) }
        block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }
    },
)

class HaimeviskaBricksStairsBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { StairBlock(BRICKS.block.await().defaultBlockState(), createPlankSettings()) },
    ::initPlanksStairsHaimeviskaBlock,
    {
        registerBlockFamily(BRICKS.block) { it.stairs(block()) }
        block.registerDefaultLootTableGeneration()
    },
)

class HaimeviskaSaplingBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(
    configuration,
    { SaplingBlock(createTreeGrower(MirageFairy2024.identifier("haimeviska_sapling")), createSaplingSettings()) },
    ::initSaplingHaimeviskaBlock,
    {
        block.registerDefaultLootTableGeneration()
        item.registerComposterInput(0.3F)
    },
)
