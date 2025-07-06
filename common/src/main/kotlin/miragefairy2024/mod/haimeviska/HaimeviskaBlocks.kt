package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerHarvestNotation
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.on
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.randomBoolean
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerFoliageColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRedirectColorProvider
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import mirrg.kotlin.hydrogen.atMost
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.BlockSetTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.block.type.WoodTypeBuilder
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.BlockFamily
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.FenceBlock
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.WoodType
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult
import java.util.Optional
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.util.ParticleUtils as ParticleUtil
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.block.RotatedPillarBlock as PillarBlock
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.block.state.StateDefinition as StateManager
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument as Instrument
import net.minecraft.world.level.material.PushReaction as PistonBehavior
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount as ApplyBonusLootFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition as RandomChanceLootCondition

lateinit var HAIMEVISKA_BLOCK_SET_TYPE: BlockSetType
lateinit var HAIMEVISKA_WOOD_TYPE: WoodType

class HaimeviskaBlockCard(
    val path: String,
    val name: EnJa,
    val poemList: PoemList,
    blockCreator: suspend () -> Block,
    val initializer: context(ModContext)(HaimeviskaBlockCard) -> Unit,
) {
    companion object {
        val entries = mutableListOf<HaimeviskaBlockCard>()
        private operator fun HaimeviskaBlockCard.not() = apply { entries += this }

        val LEAVES = !HaimeviskaBlockCard(
            "haimeviska_leaves", EnJa("Haimeviska Leaves", "ハイメヴィスカの葉"),
            PoemList(1).poem(EnJa("All original flowers are consumed by ivy", "妖精になれる花、なれない花。")),
            { HaimeviskaLeavesBlock(createLeavesSettings()) }, ::initLeavesHaimeviskaBlock,
        )
        val LOG = !HaimeviskaBlockCard(
            "haimeviska_log", EnJa("Haimeviska Log", "ハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Symbiosis with parasitic Mirages", "妖精の滲み込んだ樹。"))
                .description(EnJa("Can be incised with a sword", "剣を使って傷を付けられる")),
            { HaimeviskaLogBlock(createLogSettings()) }, initLogHaimeviskaBlock(null),
        )
        val WOOD = !HaimeviskaBlockCard(
            "haimeviska_wood", EnJa("Haimeviska Wood", "ハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Hydraulic communication system", "ウィスプたちの集合知。")),
            { PillarBlock(createLogSettings(wood = true)) }, initLogHaimeviskaBlock({ LOG }, wood = true),
        )
        val STRIPPED_LOG = !HaimeviskaBlockCard(
            "stripped_haimeviska_log", EnJa("Stripped Haimeviska Log", "樹皮を剥いだハイメヴィスカの原木"),
            PoemList(1).poem(EnJa("Something lacking the essence", "ぬぐわれたペルソナ。")),
            { PillarBlock(createLogSettings(stripped = true)) }, initLogHaimeviskaBlock(null, stripped = true),
        )
        val STRIPPED_WOOD = !HaimeviskaBlockCard(
            "stripped_haimeviska_wood", EnJa("Stripped Haimeviska Wood", "樹皮を剥いだハイメヴィスカの木"),
            PoemList(1).poem(EnJa("Loss of self", "寄生蔦からの解放。")),
            { PillarBlock(createLogSettings(stripped = true, wood = true)) }, initLogHaimeviskaBlock({ STRIPPED_LOG }, stripped = true, wood = true),
        )
        val INCISED_LOG = !HaimeviskaBlockCard(
            "incised_haimeviska_log", EnJa("Incised Haimeviska Log", "傷の付いたハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("Do fairy trees have qualia of pain?", "動物を守るということ。"))
                .description(EnJa("Produces sap over time", "時間経過で樹液を生産")),
            { IncisedHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock,
        )
        val DRIPPING_LOG = !HaimeviskaBlockCard(
            "dripping_haimeviska_log", EnJa("Dripping Haimeviska Log", "滴るハイメヴィスカの原木"),
            PoemList(1)
                .poem(EnJa("A spirit named 'glucose'", "霊界より降りしもの。"))
                .description(EnJa("Harvest sap when used", "使用時、樹液を収穫")),
            { DrippingHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock,
        )
        val HOLLOW_LOG = !HaimeviskaBlockCard(
            "hollow_haimeviska_log", EnJa("Hollow Haimeviska Log", "ハイメヴィスカの樹洞"),
            PoemList(1).poem(EnJa("Auric conceptual attractor", "限界巡回アステリア。")),
            { HollowHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock,
        )
        val PLANKS = !HaimeviskaBlockCard(
            "haimeviska_planks", EnJa("Haimeviska Planks", "ハイメヴィスカの板材"),
            PoemList(1).poem(EnJa("Flexible and friendly, good for interior", "考える、壁。")),
            { Block(createPlankSettings()) }, ::initPlanksHaimeviskaBlock,
        )
        val SLAB = !HaimeviskaBlockCard(
            "haimeviska_slab", EnJa("Haimeviska Slab", "ハイメヴィスカのハーフブロック"),
            PoemList(1).poem(EnJa("Searching for another personality.", "二重思考の側頭葉。")),
            { SlabBlock(createPlankSettings()) }, ::initPlanksSlabHaimeviskaBlock,
        )
        val STAIRS = !HaimeviskaBlockCard(
            "haimeviska_stairs", EnJa("Haimeviska Stairs", "ハイメヴィスカの階段"),
            PoemList(1).poem(EnJa("Step that pierces the sky", "情緒体を喰らう頂となれ。")),
            { StairBlock(PLANKS.block.await().defaultBlockState(), createPlankSettings()) }, ::initPlanksStairsHaimeviskaBlock,
        )
        val FENCE = !HaimeviskaBlockCard(
            "haimeviska_fence", EnJa("Haimeviska Fence", "ハイメヴィスカのフェンス"),
            PoemList(1).poem(EnJa("Personality flowing through the xylem", "樹のなかに住む。")),
            { FenceBlock(createPlankSettings()) }, ::initPlanksFenceHaimeviskaBlock,
        )
        val FENCE_GATE = !HaimeviskaBlockCard(
            "haimeviska_fence_gate", EnJa("Haimeviska Fence Gate", "ハイメヴィスカのフェンスゲート"),
            PoemList(1).poem(EnJa("It chose this path of its own will", "知性の邂逅。")),
            { FenceGateBlock(HAIMEVISKA_WOOD_TYPE, createPlankSettings(sound = false).forceSolidOn()) }, ::initPlanksFenceGateHaimeviskaBlock,
        )
        val BUTTON = !HaimeviskaBlockCard(
            "haimeviska_button", EnJa("Haimeviska Button", "ハイメヴィスカのボタン"),
            PoemList(1).poem(EnJa("What is this soft and warm thing?", "指先の感触。")),
            { ButtonBlock(HAIMEVISKA_BLOCK_SET_TYPE, 30, AbstractBlock.Properties.of().noCollission().strength(0.5F).pushReaction(PistonBehavior.DESTROY)) }, ::initPlanksButtonHaimeviskaBlock,

            )
        val PRESSURE_PLATE = !HaimeviskaBlockCard(
            "haimeviska_pressure_plate", EnJa("Haimeviska Pressure Plate", "ハイメヴィスカの感圧板"),
            PoemList(1).poem(EnJa("Creature with the name of a machine", "感応と感覚の違い。")),
            { PressurePlateBlock(HAIMEVISKA_BLOCK_SET_TYPE, createBaseWoodSetting(sound = false).forceSolidOn().noCollission().strength(0.5F).pushReaction(PistonBehavior.DESTROY)) }, ::initPlanksPressurePlateHaimeviskaBlock,

            )
        val SAPLING = !HaimeviskaBlockCard(
            "haimeviska_sapling", EnJa("Haimeviska Sapling", "ハイメヴィスカの苗木"),
            PoemList(1).poem(EnJa("Assembling molecules with Ergs", "第二の葉緑体。")),
            { SaplingBlock(createTreeGrower(MirageFairy2024.identifier("haimeviska_sapling")), createSaplingSettings()) }, ::initSaplingHaimeviskaBlock,
        )
    }

    val identifier = MirageFairy2024.identifier(path)
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { blockCreator() }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
}

private fun createLeavesSettings() = AbstractBlock.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(BlockSoundGroup.GRASS).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(PistonBehavior.DESTROY).isRedstoneConductor(Blocks::never)
private fun createBaseWoodSetting(sound: Boolean = true) = AbstractBlock.Properties.of().instrument(Instrument.BASS).let { if (sound) it.sound(BlockSoundGroup.WOOD) else it }.ignitedByLava()
private fun createLogSettings(stripped: Boolean = false, wood: Boolean = false) = createBaseWoodSetting().strength(2.0F).mapColor { if (stripped) MapColor.RAW_IRON else if (wood) MapColor.TERRACOTTA_ORANGE else if (it.getValue(PillarBlock.AXIS) === Direction.Axis.Y) MapColor.RAW_IRON else MapColor.TERRACOTTA_ORANGE }
private fun createSpecialLogSettings() = createBaseWoodSetting().strength(2.0F).mapColor(MapColor.RAW_IRON)
private fun createPlankSettings(sound: Boolean = true) = createBaseWoodSetting(sound = sound).strength(2.0F, 3.0F).mapColor(MapColor.RAW_IRON)
private fun createSaplingSettings() = AbstractBlock.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(BlockSoundGroup.GRASS).pushReaction(PistonBehavior.DESTROY)

private fun createTreeGrower(identifier: ResourceLocation) = TreeGrower(identifier.string, Optional.empty(), Optional.of(HAIMEVISKA_CONFIGURED_FEATURE_KEY), Optional.empty())

context(ModContext)
private fun initLeavesHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerVariantsBlockStateGeneration {
        val normal = BlockStateVariant(model = "block/" * card.block().getIdentifier())
        listOf(
            propertiesOf(HaimeviskaLeavesBlock.CHARGED with true) with normal.with(model = "block/charged_" * card.block().getIdentifier()),
            propertiesOf(HaimeviskaLeavesBlock.CHARGED with false) with normal.with(model = "block/uncharged_" * card.block().getIdentifier()),
        )
    }
    registerModelGeneration({ "block/charged_" * card.block().getIdentifier() }, { chargedHaimeviskaLeavesTexturedModelFactory.get(card.block()) })
    registerModelGeneration({ "block/uncharged_" * card.block().getIdentifier() }, { unchargedHaimeviskaLeavesTexturedModelFactory.get(card.block()) })
    card.item.registerModelGeneration(Model("block/charged_" * card.identifier))
    card.block.registerCutoutRenderLayer()
    card.block.registerFoliageColorProvider()
    card.item.registerRedirectColorProvider()

    // 性質
    card.block.registerFlammable(30, 30)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.LEAVES }
    card.item.registerItemTagGeneration { ItemTags.LEAVES }
    card.block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_HOE }

}

private fun initLogHaimeviskaBlock(logCard: (() -> HaimeviskaBlockCard)?, stripped: Boolean = false, wood: Boolean = false): context(ModContext) (card: HaimeviskaBlockCard) -> Unit {
    return { card ->

        // レンダリング
        DataGenerationEvents.onGenerateBlockModel {
            if (wood) {
                it.woodProvider((if (logCard != null) logCard() else card).block()).wood(card.block())
            } else {
                it.woodProvider((if (logCard != null) logCard() else card).block()).logWithHorizontal(card.block())
            }
        }

        // 性質
        card.block.registerFlammable(5, 5)

        // タグ
        if (!stripped && !wood) card.block.registerBlockTagGeneration { BlockTags.OVERWORLD_NATURAL_LOGS }
        card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS_BLOCK_TAG }
        if (stripped) card.block.registerBlockTagGeneration { TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "stripped_logs")) }
        card.item.registerItemTagGeneration { HAIMEVISKA_LOGS_ITEM_TAG }
        if (stripped) card.item.registerItemTagGeneration { TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "stripped_logs")) }

    }
}

context(ModContext)
private fun initHorizontalFacingLogHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block().getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
    card.block.registerModelGeneration {
        Models.CUBE_ORIENTABLE.with(
            TextureKey.TOP to "block/" * HaimeviskaBlockCard.LOG.block().getIdentifier() * "_top",
            TextureKey.SIDE to "block/" * HaimeviskaBlockCard.LOG.block().getIdentifier(),
            TextureKey.FRONT to "block/" * it.getIdentifier(),
        )
    }

    // 性質
    card.block.registerFlammable(5, 5)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.OVERWORLD_NATURAL_LOGS }
    card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS_BLOCK_TAG }
    card.item.registerItemTagGeneration { HAIMEVISKA_LOGS_ITEM_TAG }

}

context(ModContext)
private fun initPlanksHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.PLANKS }
    card.item.registerItemTagGeneration { ItemTags.PLANKS }

}

context(ModContext)
private fun initPlanksSlabHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.WOODEN_SLABS }
    card.item.registerItemTagGeneration { ItemTags.WOODEN_SLABS }

}

context(ModContext)
private fun initPlanksStairsHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.WOODEN_STAIRS }
    card.item.registerItemTagGeneration { ItemTags.WOODEN_STAIRS }

}

context(ModContext)
private fun initPlanksFenceHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.WOODEN_FENCES }
    card.item.registerItemTagGeneration { ItemTags.WOODEN_FENCES }

}

context(ModContext)
private fun initPlanksFenceGateHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.FENCE_GATES }
    card.item.registerItemTagGeneration { ItemTags.FENCE_GATES }
    card.block.registerBlockTagGeneration { TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "fence_gates/wooden")) }
    card.item.registerItemTagGeneration { TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "fence_gates/wooden")) }

}

context(ModContext)
private fun initPlanksButtonHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.WOODEN_BUTTONS }
    card.item.registerItemTagGeneration { ItemTags.WOODEN_BUTTONS }

}

context(ModContext)
private fun initPlanksPressurePlateHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.WOODEN_PRESSURE_PLATES }
    card.item.registerItemTagGeneration { ItemTags.WOODEN_PRESSURE_PLATES }

}

context(ModContext)
private fun initSaplingHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerSingletonBlockStateGeneration()
    card.block.registerModelGeneration {
        Models.CROSS.with(
            TextureKey.CROSS to "block/" * it.getIdentifier(),
        )
    }
    card.item.registerBlockGeneratedModelGeneration(card.block)
    card.block.registerCutoutRenderLayer()

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.SAPLINGS }
    card.item.registerItemTagGeneration { ItemTags.SAPLINGS }

}


val HAIMEVISKA_LOGS_BLOCK_TAG: TagKey<Block> = TagKey.create(Registries.BLOCK, MirageFairy2024.identifier("haimeviska_logs"))
val HAIMEVISKA_LOGS_ITEM_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, MirageFairy2024.identifier("haimeviska_logs"))

context(ModContext)
fun initHaimeviskaBlocks() {

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_leaves")) { HaimeviskaLeavesBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("haimeviska_log")) { HaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("incised_haimeviska_log")) { IncisedHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("dripping_haimeviska_log")) { DrippingHaimeviskaLogBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("hollow_haimeviska_log")) { HollowHaimeviskaLogBlock.CODEC }.register()

    HAIMEVISKA_BLOCK_SET_TYPE = BlockSetTypeBuilder().register(MirageFairy2024.identifier("haimeviska"))
    HAIMEVISKA_WOOD_TYPE = WoodTypeBuilder().register(MirageFairy2024.identifier("haimeviska"), HAIMEVISKA_BLOCK_SET_TYPE)

    HaimeviskaBlockCard.entries.forEach { card ->

        // 登録
        card.block.register()
        card.item.register()

        // カテゴリ
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // テキスト
        card.block.enJa(card.name)
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.initializer(this@ModContext, card)
    }

    DataGenerationEvents.onGenerateBlockModel {
        val family = BlockFamily.Builder(HaimeviskaBlockCard.PLANKS.block())
            .slab(HaimeviskaBlockCard.SLAB.block())
            .stairs(HaimeviskaBlockCard.STAIRS.block())
            .fence(HaimeviskaBlockCard.FENCE.block())
            .fenceGate(HaimeviskaBlockCard.FENCE_GATE.block())
            .button(HaimeviskaBlockCard.BUTTON.block())
            .pressurePlate(HaimeviskaBlockCard.PRESSURE_PLATE.block())
            .family
        it.family(family.baseBlock).generateFor(family)
    }
    DataGenerationEvents.onGenerateRecipe {
        val family = BlockFamily.Builder(HaimeviskaBlockCard.PLANKS.block())
            .slab(HaimeviskaBlockCard.SLAB.block())
            .stairs(HaimeviskaBlockCard.STAIRS.block())
            .fence(HaimeviskaBlockCard.FENCE.block())
            .fenceGate(HaimeviskaBlockCard.FENCE_GATE.block())
            .button(HaimeviskaBlockCard.BUTTON.block())
            .pressurePlate(HaimeviskaBlockCard.PRESSURE_PLATE.block())
            .family
        RecipeProvider.generateRecipes(it, family, FeatureFlagSet.of(FeatureFlags.VANILLA))
    }

    // ドロップ
    HaimeviskaBlockCard.LEAVES.block.registerLootTableGeneration { it, _ ->
        it.createLeavesDrops(HaimeviskaBlockCard.LEAVES.block(), HaimeviskaBlockCard.SAPLING.block(), 0.05F / 4F, 0.0625F / 4F, 0.083333336F / 4F, 0.1F / 4F)
    }
    HaimeviskaBlockCard.LOG.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.STRIPPED_LOG.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.WOOD.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.STRIPPED_WOOD.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.INCISED_LOG.block.registerLootTableGeneration { provider, _ ->
        LootTable(
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.INCISED_LOG.item())) {
                `when`(provider.hasSilkTouch())
            },
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.LOG.item())) {
                `when`(provider.doesNotHaveSilkTouch())
            },
        ) {
            provider.applyExplosionDecay(HaimeviskaBlockCard.INCISED_LOG.block(), this)
        }
    }
    HaimeviskaBlockCard.DRIPPING_LOG.block.registerLootTableGeneration { provider, registries ->
        LootTable(
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.DRIPPING_LOG.item())) {
                `when`(provider.hasSilkTouch())
            },
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.LOG.item())) {
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
            provider.applyExplosionDecay(HaimeviskaBlockCard.DRIPPING_LOG.block(), this)
        }
    }
    HaimeviskaBlockCard.HOLLOW_LOG.block.registerLootTableGeneration { provider, registries ->
        LootTable(
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.HOLLOW_LOG.item())) {
                `when`(provider.hasSilkTouch())
            },
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.LOG.item())) {
                `when`(provider.doesNotHaveSilkTouch())
            },
            LootPool(ItemLootPoolEntry(MaterialCard.FRACTAL_WISP.item()) {
                apply(ApplyBonusLootFunction.addUniformBonusCount(registries[Registries.ENCHANTMENT, Enchantments.FORTUNE]))
            }) {
                `when`(provider.doesNotHaveSilkTouch())
            },
        ) {
            provider.applyExplosionDecay(HaimeviskaBlockCard.HOLLOW_LOG.block(), this)
        }
    }
    HaimeviskaBlockCard.PLANKS.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.SLAB.block.registerLootTableGeneration { it, _ -> it.createSlabItemTable(HaimeviskaBlockCard.SLAB.block()) }
    HaimeviskaBlockCard.STAIRS.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.FENCE.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.FENCE_GATE.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.BUTTON.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.PRESSURE_PLATE.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.SAPLING.block.registerDefaultLootTableGeneration()

    // レシピ
    HaimeviskaBlockCard.LEAVES.item.registerComposterInput(0.3F)
    HaimeviskaBlockCard.SAPLING.item.registerComposterInput(0.3F)
    registerShapelessRecipeGeneration(HaimeviskaBlockCard.PLANKS.item, 4) {
        requires(HaimeviskaBlockCard.LOG.item())
    } on HaimeviskaBlockCard.LOG.item from HaimeviskaBlockCard.LOG.item
    HaimeviskaBlockCard.DRIPPING_LOG.item.registerHarvestNotation(MaterialCard.HAIMEVISKA_SAP.item, MaterialCard.HAIMEVISKA_ROSIN.item)
    HaimeviskaBlockCard.HOLLOW_LOG.item.registerHarvestNotation(MaterialCard.FRACTAL_WISP.item)
    ModEvents.onInitialize {
        StrippableBlockRegistry.register(HaimeviskaBlockCard.LOG.block(), HaimeviskaBlockCard.STRIPPED_LOG.block())
    }
    ModEvents.onInitialize {
        StrippableBlockRegistry.register(HaimeviskaBlockCard.WOOD.block(), HaimeviskaBlockCard.STRIPPED_WOOD.block())
    }
    registerShapedRecipeGeneration(HaimeviskaBlockCard.WOOD.item, 3) {
        pattern("##")
        pattern("##")
        define('#', HaimeviskaBlockCard.LOG.item())
    } on HaimeviskaBlockCard.LOG.item
    registerShapedRecipeGeneration(HaimeviskaBlockCard.STRIPPED_WOOD.item, 3) {
        pattern("##")
        pattern("##")
        define('#', HaimeviskaBlockCard.STRIPPED_LOG.item())
    } on HaimeviskaBlockCard.LOG.item

    // タグ
    HAIMEVISKA_LOGS_BLOCK_TAG.registerBlockTagGeneration { BlockTags.LOGS_THAT_BURN }
    HAIMEVISKA_LOGS_ITEM_TAG.registerItemTagGeneration { ItemTags.LOGS_THAT_BURN }

}


class HaimeviskaLeavesBlock(settings: Properties) : LeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLeavesBlock> = simpleCodec(::HaimeviskaLeavesBlock)
        val CHARGED: BooleanProperty = BooleanProperty.create("charged")
    }

    override fun codec() = CODEC

    init {
        registerDefaultState(defaultBlockState().setValue(CHARGED, true))
    }

    override fun createBlockStateDefinition(builder: StateManager.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CHARGED)
    }

    override fun isRandomlyTicking(state: BlockState) = super.isRandomlyTicking(state) || !state.getValue(CHARGED)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        super.randomTick(state, world, pos, random)
        if (!state.getValue(CHARGED)) {
            if (random.randomBoolean(15, world.getMaxLocalRawBrightness(pos))) {
                world.setBlock(pos, state.setValue(CHARGED, true), Block.UPDATE_CLIENTS)
            }
        }
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: Random) {
        super.animateTick(state, world, pos, random)
        if (random.nextInt(20) == 0) {
            val blockPos = pos.below()
            if (!isFaceFull(world.getBlockState(blockPos).getCollisionShape(world, blockPos), Direction.UP)) {
                ParticleUtil.spawnParticleBelow(world, pos, random, ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType)
            }
        }
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class HaimeviskaLogBlock(settings: Properties) : PillarBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLogBlock> = simpleCodec(::HaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (state.getValue(AXIS) != Direction.Axis.Y) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 縦方向でなければスルー
        if (!stack.`is`(ItemTags.SWORDS)) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 剣でなければスルー
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = if (hitResult.direction.axis === Direction.Axis.Y) player.direction.opposite else hitResult.direction

        // 加工
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block().defaultBlockState().setValue(HorizontalFacingBlock.FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)
        player.awardStat(Stats.ITEM_USED.get(stack.item))

        // エフェクト
        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F)

        return ItemInteractionResult.CONSUME
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class IncisedHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedHaimeviskaLogBlock> = simpleCodec(::IncisedHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (random.nextInt(100) == 0) {
            world.setBlock(pos, HaimeviskaBlockCard.DRIPPING_LOG.block().defaultBlockState().setValue(FACING, state.getValue(FACING)), Block.UPDATE_ALL)
        }
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class DrippingHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingHaimeviskaLogBlock> = simpleCodec(::DrippingHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = state.getValue(FACING)

        // 消費
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block().defaultBlockState().setValue(FACING, direction), Block.UPDATE_ALL or Block.UPDATE_IMMEDIATE)

        fun drop(item: Item, count: Double) {
            val actualCount = level.random.randomInt(count) atMost item.defaultMaxStackSize
            if (actualCount <= 0) return
            val itemStack = item.createItemStack(actualCount)
            val itemEntity = ItemEntity(level, pos.x + 0.5 + direction.stepX * 0.65, pos.y + 0.1, pos.z + 0.5 + direction.stepZ * 0.65, itemStack)
            itemEntity.setDeltaMovement(0.05 * direction.stepX + level.random.nextDouble() * 0.02, 0.05, 0.05 * direction.stepZ + level.random.nextDouble() * 0.02)
            level.addFreshEntity(itemEntity)
        }

        // 生産
        val fortune = EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess()[Registries.ENCHANTMENT, Enchantments.FORTUNE], stack)
        drop(MaterialCard.HAIMEVISKA_SAP.item(), 1.0 + 0.25 * fortune) // ハイメヴィスカの樹液
        drop(MaterialCard.HAIMEVISKA_ROSIN.item(), 0.03 + 0.01 * fortune) // 妖精の木の涙

        // エフェクト
        level.playSound(null, pos, SoundEvents.SLIME_JUMP, SoundCategory.BLOCKS, 0.75F, 1.0F + 0.5F * level.random.nextFloat())

        return ItemInteractionResult.CONSUME
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: Random) {
        if (random.nextFloat() >= 0.2F) return

        val direction = state.getValue(FACING)
        val destBlockPos = pos.relative(direction)
        val destBlockState = world.getBlockState(destBlockPos)
        val destShape = destBlockState.getCollisionShape(world, destBlockPos)
        val hasSpace = when (direction) {
            Direction.NORTH -> destShape.max(Direction.Axis.Z) < 1.0
            Direction.SOUTH -> destShape.min(Direction.Axis.Z) > 0.0
            Direction.WEST -> destShape.max(Direction.Axis.X) < 1.0
            Direction.EAST -> destShape.min(Direction.Axis.X) > 0.0
            else -> throw IllegalStateException()
        }
        if (!(hasSpace || !destBlockState.isCollisionShapeFullBlock(world, destBlockPos))) return

        val position = random.nextInt(2)
        val x = when (position) {
            0 -> (7.0 + 7.0 * world.random.nextDouble()) / 16.0
            else -> (2.0 + 8.0 * world.random.nextDouble()) / 16.0
        }
        val y = when (position) {
            0 -> 12.0 / 16.0
            else -> 5.0 / 16.0
        }
        val z = 17.0 / 16.0

        val (x2, z2) = when (direction) {
            Direction.NORTH -> Pair(1.0 - x, 1.0 - z)
            Direction.EAST -> Pair(0.0 + z, 1.0 - x)
            Direction.SOUTH -> Pair(0.0 + x, 0.0 + z)
            Direction.WEST -> Pair(1.0 - z, 0.0 + x)
            else -> throw IllegalStateException()
        }

        world.addParticle(
            ParticleTypeCard.DRIPPING_HAIMEVISKA_SAP.particleType,
            pos.x + x2,
            pos.y + y - 1.0 / 16.0,
            pos.z + z2,
            0.0,
            0.0,
            0.0,
        )
    }
}

class HollowHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<HollowHaimeviskaLogBlock> = simpleCodec(::HollowHaimeviskaLogBlock)
    }

    override fun codec() = CODEC
}
