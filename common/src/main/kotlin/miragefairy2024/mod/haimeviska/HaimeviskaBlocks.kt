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

class HaimeviskaBlockCard(
    val path: String,
    val name: EnJa,
    val poemList: PoemList,
    blockCreator: suspend () -> Block,
    val initializer: context(ModContext)(HaimeviskaBlockCard) -> Unit,
    val extraInitializer: context(ModContext) HaimeviskaBlockCard.() -> Unit,
) {
    companion object {
        val entries = mutableListOf<HaimeviskaBlockCard>()
        private operator fun HaimeviskaBlockCard.not() = apply { entries += this }

        val LEAVES = !HaimeviskaBlockCard(
            "haimeviska_leaves", EnJa("Haimeviska Leaves", "ハイメヴィスカの葉"),
            PoemList(1).poem(EnJa("All original flowers are consumed by ivy", "妖精になれる花、なれない花。")),
            { HaimeviskaLeavesBlock(createLeavesSettings()) }, ::initLeavesHaimeviskaBlock,
        ) {
            block.registerLootTableGeneration { it, _ ->
                it.createLeavesDrops(block(), SAPLING.block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F)
            }
            item.registerComposterInput(0.3F)
        }
        val LOG = !HaimeviskaBlockCard(
            "haimeviska_log", EnJa("Haimeviska Log", "ハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Symbiosis with parasitic Mirages", "妖精の滲み込んだ樹。"))
                .description(EnJa("Can be incised with a sword", "剣を使って傷を付けられる")),
            { HaimeviskaLogBlock(createLogSettings()) }, initLogHaimeviskaBlock(null),
        ) {
            block.registerDefaultLootTableGeneration()
        }
        val WOOD = !HaimeviskaBlockCard(
            "haimeviska_wood", EnJa("Haimeviska Wood", "ハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Hydraulic communication system", "ウィスプたちの集合知。")),
            { PillarBlock(createLogSettings(wood = true)) }, initLogHaimeviskaBlock({ LOG }, wood = true),
        ) {
            block.registerDefaultLootTableGeneration()
            registerShapedRecipeGeneration(item, 3) {
                pattern("##")
                pattern("##")
                define('#', LOG.item())
            } on LOG.item
        }
        val STRIPPED_LOG = !HaimeviskaBlockCard(
            "stripped_haimeviska_log", EnJa("Stripped Haimeviska Log", "樹皮を剥いだハイメヴィスカの原木"),
            PoemList(1).poem(EnJa("Something lacking the essence", "ぬぐわれたペルソナ。")),
            { PillarBlock(createLogSettings(stripped = true)) }, initLogHaimeviskaBlock(null, stripped = true),
        ) {
            block.registerDefaultLootTableGeneration()
            ModEvents.onInitialize {
                StrippableBlockRegistry.register(LOG.block(), block())
            }
        }
        val STRIPPED_WOOD = !HaimeviskaBlockCard(
            "stripped_haimeviska_wood", EnJa("Stripped Haimeviska Wood", "樹皮を剥いだハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Loss of self", "寄生蔦からの解放。")),
            { PillarBlock(createLogSettings(stripped = true, wood = true)) }, initLogHaimeviskaBlock({ STRIPPED_LOG }, stripped = true, wood = true),
        ) {
            block.registerDefaultLootTableGeneration()
            registerShapedRecipeGeneration(item, 3) {
                pattern("##")
                pattern("##")
                define('#', STRIPPED_LOG.item())
            } on STRIPPED_LOG.item
            ModEvents.onInitialize {
                StrippableBlockRegistry.register(WOOD.block(), block())
            }
        }
        val INCISED_LOG = !HaimeviskaBlockCard(
            "incised_haimeviska_log", EnJa("Incised Haimeviska Log", "傷の付いたハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Do fairy trees have qualia of pain?", "動物を守るということ。"))
                .description(EnJa("Produces sap over time", "時間経過で樹液を生産")),
            { IncisedHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock,
        ) {
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
        }
        val DRIPPING_LOG = !HaimeviskaBlockCard(
            "dripping_haimeviska_log", EnJa("Dripping Haimeviska Log", "滴るハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("A spirit named 'glucose'", "霊界より降りしもの。"))
                .description(EnJa("Harvest sap when used", "使用時、樹液を収穫")),
            { DrippingHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock,
        ) {
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
        }
        val HOLLOW_LOG = !HaimeviskaBlockCard(
            "hollow_haimeviska_log", EnJa("Hollow Haimeviska Log", "ハイメヴィスカの樹洞"),
            PoemList(1).poem(EnJa("Auric conceptual attractor", "限界巡回アステリア。")),
            { HollowHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock,
        ) {
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
        }
        val PLANKS = !HaimeviskaBlockCard(
            "haimeviska_planks", EnJa("Haimeviska Planks", "ハイメヴィスカの板材"),
            PoemList(1).poem(EnJa("Flexible and friendly, good for interior", "考える、壁。")),
            { Block(createPlankSettings()) }, ::initPlanksHaimeviskaBlock,
        ) {
            block.registerDefaultLootTableGeneration()
            registerShapelessRecipeGeneration(item, 4) {
                requires(LOG.item())
            } on LOG.item from LOG.item
        }
        val SLAB = !HaimeviskaBlockCard(
            "haimeviska_slab", EnJa("Haimeviska Slab", "ハイメヴィスカのハーフブロック"),
            PoemList(1).poem(EnJa("Searching for another personality.", "半人前の側頭葉。")),
            { SlabBlock(createPlankSettings()) }, ::initPlanksSlabHaimeviskaBlock,
        ) {
            registerBlockFamily(PLANKS.block) { it.slab(block()) }
            block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }
        }
        val STAIRS = !HaimeviskaBlockCard(
            "haimeviska_stairs", EnJa("Haimeviska Stairs", "ハイメヴィスカの階段"),
            PoemList(1).poem(EnJa("Step that pierces the sky", "情緒体を喰らう頂となれ。")),
            { StairBlock(PLANKS.block.await().defaultBlockState(), createPlankSettings()) }, ::initPlanksStairsHaimeviskaBlock,
        ) {
            registerBlockFamily(PLANKS.block) { it.stairs(block()) }
            block.registerDefaultLootTableGeneration()
        }
        val FENCE = !HaimeviskaBlockCard(
            "haimeviska_fence", EnJa("Haimeviska Fence", "ハイメヴィスカのフェンス"),
            PoemList(1).poem(EnJa("Personality flowing through the xylem", "樹のなかに住む。")),
            { FenceBlock(createPlankSettings()) }, ::initPlanksFenceHaimeviskaBlock,
        ) {
            registerBlockFamily(PLANKS.block) { it.fence(block()) }
            block.registerDefaultLootTableGeneration()
        }
        val FENCE_GATE = !HaimeviskaBlockCard(
            "haimeviska_fence_gate", EnJa("Haimeviska Fence Gate", "ハイメヴィスカのフェンスゲート"),
            PoemList(1).poem(EnJa("It chose this path of its own will", "知性の邂逅。")),
            { FenceGateBlock(HAIMEVISKA_WOOD_TYPE, createPlankSettings(sound = false).forceSolidOn()) }, ::initPlanksFenceGateHaimeviskaBlock,
        ) {
            registerBlockFamily(PLANKS.block) { it.fenceGate(block()) }
            block.registerDefaultLootTableGeneration()
        }
        val BUTTON = !HaimeviskaBlockCard(
            "haimeviska_button", EnJa("Haimeviska Button", "ハイメヴィスカのボタン"),
            PoemList(1).poem(EnJa("What is this soft and warm thing?", "指先の感触。")),
            { ButtonBlock(HAIMEVISKA_BLOCK_SET_TYPE, 30, AbstractBlock.Properties.of().noCollission().strength(0.5F).pushReaction(PistonBehavior.DESTROY)) }, ::initPlanksButtonHaimeviskaBlock,
        ) {
            registerBlockFamily(PLANKS.block) { it.button(block()) }
            block.registerDefaultLootTableGeneration()
        }
        val PRESSURE_PLATE = !HaimeviskaBlockCard(
            "haimeviska_pressure_plate", EnJa("Haimeviska Pressure Plate", "ハイメヴィスカの感圧板"),
            PoemList(1).poem(EnJa("Creature with the name of a machine", "感応と感覚の違い。")),
            { PressurePlateBlock(HAIMEVISKA_BLOCK_SET_TYPE, createBaseWoodSetting(sound = false).forceSolidOn().noCollission().strength(0.5F).pushReaction(PistonBehavior.DESTROY)) }, ::initPlanksPressurePlateHaimeviskaBlock,
        ) {
            registerBlockFamily(PLANKS.block) { it.pressurePlate(block()) }
            block.registerDefaultLootTableGeneration()
        }
        val BRICKS = !HaimeviskaBlockCard(
            "haimeviska_bricks", EnJa("Haimeviska Bricks", "ハイメヴィスカレンガ"),
            PoemList(1).poem(EnJa("An ecosystem called 'civilization'", "人がもたらした原生林。")),
            { Block(createPlankSettings()) }, ::initPlanksHaimeviskaBlock,
        ) {
            block.registerDefaultLootTableGeneration()
            registerShapedRecipeGeneration(item, 4) {
                pattern("##")
                pattern("##")
                define('#', PLANKS.item())
            } on PLANKS.item from PLANKS.item
        }
        val BRICKS_SLAB = !HaimeviskaBlockCard(
            "haimeviska_bricks_slab", EnJa("Haimeviska Bricks Slab", "ハイメヴィスカレンガのハーフブロック"),
            PoemList(1).poem(EnJa("Extremely modularized memory", "ひとまわり細かくなった私。")),
            { SlabBlock(createPlankSettings()) }, ::initPlanksSlabHaimeviskaBlock,
        ) {
            registerBlockFamily(BRICKS.block) { it.slab(block()) }
            block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(block()) }
        }
        val BRICKS_STAIRS = !HaimeviskaBlockCard(
            "haimeviska_bricks_stairs", EnJa("Haimeviska Bricks Stairs", "ハイメヴィスカレンガの階段"),
            PoemList(1).poem(EnJa("Forgotten paths of the technology", "生体工学の歩み。")),
            { StairBlock(BRICKS.block.await().defaultBlockState(), createPlankSettings()) }, ::initPlanksStairsHaimeviskaBlock,
        ) {
            registerBlockFamily(BRICKS.block) { it.stairs(block()) }
            block.registerDefaultLootTableGeneration()
        }
        val SAPLING = !HaimeviskaBlockCard(
            "haimeviska_sapling", EnJa("Haimeviska Sapling", "ハイメヴィスカの苗木"),
            PoemList(1).poem(EnJa("Assembling molecules with Ergs", "第二の葉緑体。")),
            { SaplingBlock(createTreeGrower(MirageFairy2024.identifier("haimeviska_sapling")), createSaplingSettings()) }, ::initSaplingHaimeviskaBlock,
        ) {
            block.registerDefaultLootTableGeneration()
            item.registerComposterInput(0.3F)
        }
    }

    val identifier = MirageFairy2024.identifier(path)
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
        block.enJa(name)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

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

}

context(ModContext)
private fun registerBlockFamily(baseBlock: () -> Block, initializer: (BlockFamily.Builder) -> BlockFamily.Builder) {
    val family by lazy { initializer(BlockFamily.Builder(baseBlock())).family }
    DataGenerationEvents.onGenerateBlockModel {
        it.family(family.baseBlock).generateFor(family)
    }
    DataGenerationEvents.onGenerateRecipe {
        RecipeProvider.generateRecipes(it, family, FeatureFlagSet.of(FeatureFlags.VANILLA))
    }
}
