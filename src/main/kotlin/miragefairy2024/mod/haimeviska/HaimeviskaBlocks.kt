package miragefairy2024.mod.haimeviska

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
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
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
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
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.RotatedPillarBlock as PillarBlock
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument as Instrument
import net.minecraft.world.level.material.PushReaction as PistonBehavior
import net.minecraft.world.level.block.grower.AbstractTreeGrower as SaplingGenerator
import net.minecraft.util.ParticleUtils as ParticleUtil
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.data.loot.BlockLootSubProvider as BlockLootTableGenerator
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition as RandomChanceLootCondition
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount as ApplyBonusLootFunction
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.world.level.block.state.StateDefinition as StateManager
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.InteractionResult as ActionResult
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.Level as World

class HaimeviskaBlockCard(val configuration: Configuration, blockCreator: () -> Block, val initializer: context(ModContext)(HaimeviskaBlockCard) -> Unit) {
    companion object {
        val LEAVES = Configuration(
            "haimeviska_leaves", "Haimeviska Leaves", "ハイメヴィスカの葉",
            PoemList(1).poem("All original flowers are consumed by ivy", "妖精になれる花、なれない花。"),
        ).let { HaimeviskaBlockCard(it, { HaimeviskaLeavesBlock(createLeavesSettings()) }, ::initLeavesHaimeviskaBlock) }
        val LOG = Configuration(
            "haimeviska_log", "Haimeviska Log", "ハイメヴィスカの原木",
            PoemList(1)
                .poem("Symbiosis with parasitic Mirages", "妖精の滲み込んだ樹。")
                .description("Can be incised with a sword", "剣を使って傷を付けられる"),
        ).let { HaimeviskaBlockCard(it, { HaimeviskaLogBlock(createLogSettings()) }, ::initLogHaimeviskaBlock) }
        val INCISED_LOG = Configuration(
            "incised_haimeviska_log", "Incised Haimeviska Log", "傷の付いたハイメヴィスカの原木",
            PoemList(1)
                .poem("Do fairy trees have qualia of pain?", "動物を守るということ。")
                .description("Produces sap over time", "時間経過で樹液を生産")
        ).let { HaimeviskaBlockCard(it, { IncisedHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock) }
        val DRIPPING_LOG = Configuration(
            "dripping_haimeviska_log", "Dripping Haimeviska Log", "滴るハイメヴィスカの原木",
            PoemList(1)
                .poem("A spirit named 'glucose'", "霊界より降りしもの。")
                .description("Harvest sap when used", "使用時、樹液を収穫"),
        ).let { HaimeviskaBlockCard(it, { DrippingHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock) }
        val HOLLOW_LOG = Configuration(
            "hollow_haimeviska_log", "Hollow Haimeviska Log", "ハイメヴィスカの樹洞",
            PoemList(1).poem("Auric conceptual attractor", "限界巡回アステリア。"),
        ).let { HaimeviskaBlockCard(it, { SimpleHorizontalFacingBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock) }
        val PLANKS = Configuration(
            "haimeviska_planks", "Haimeviska Planks", "ハイメヴィスカの板材",
            PoemList(1).poem("Flexible and friendly, good for interior", "考える、壁。"),
        ).let { HaimeviskaBlockCard(it, { Block(createPlankSettings()) }, ::initPlanksHaimeviskaBlock) }
        val SAPLING = Configuration(
            "haimeviska_sapling", "Haimeviska Sapling", "ハイメヴィスカの苗木",
            PoemList(1).poem("Assembling molecules with Ergs", "第二の葉緑体。"),
        ).let { HaimeviskaBlockCard(it, { SaplingBlock(HaimeviskaSaplingGenerator(), createSaplingSettings()) }, ::initSaplingHaimeviskaBlock) }

        val entries = listOf(LEAVES, LOG, INCISED_LOG, DRIPPING_LOG, HOLLOW_LOG, PLANKS, SAPLING)
    }

    class Configuration(val path: String, val enName: String, val jaName: String, val poemList: PoemList)

    val identifier = MirageFairy2024.identifier(configuration.path)
    val block = blockCreator()
    val item = BlockItem(block, Item.Settings())
}

private fun createLeavesSettings() = AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).strength(0.2F).ticksRandomly().sounds(BlockSoundGroup.GRASS).nonOpaque().allowsSpawning(Blocks::canSpawnOnLeaves).suffocates(Blocks::never).blockVision(Blocks::never).burnable().pistonBehavior(PistonBehavior.DESTROY).solidBlock(Blocks::never)
private fun createBaseWoodSetting() = AbstractBlock.Settings.create().instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).burnable()
private fun createLogSettings() = createBaseWoodSetting().strength(2.0F).mapColor { if (it.get(PillarBlock.AXIS) === Direction.Axis.Y) MapColor.RAW_IRON_PINK else MapColor.TERRACOTTA_ORANGE }
private fun createSpecialLogSettings() = createBaseWoodSetting().strength(2.0F).mapColor(MapColor.RAW_IRON_PINK)
private fun createPlankSettings() = createBaseWoodSetting().strength(2.0F, 3.0F).mapColor(MapColor.RAW_IRON_PINK)
private fun createSaplingSettings() = AbstractBlock.Settings.create().mapColor(MapColor.DARK_GREEN).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.GRASS).pistonBehavior(PistonBehavior.DESTROY)

context(ModContext)
private fun initLeavesHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerVariantsBlockStateGeneration {
        val normal = BlockStateVariant(model = "block/" * card.block.getIdentifier())
        listOf(
            propertiesOf(HaimeviskaLeavesBlock.CHARGED with true) with normal.with(model = "block/charged_" * card.block.getIdentifier()),
            propertiesOf(HaimeviskaLeavesBlock.CHARGED with false) with normal.with(model = "block/uncharged_" * card.block.getIdentifier()),
        )
    }
    registerModelGeneration({ "block/charged_" * card.block.getIdentifier() }, { chargedHaimeviskaLeavesTexturedModelFactory.get(card.block) })
    registerModelGeneration({ "block/uncharged_" * card.block.getIdentifier() }, { unchargedHaimeviskaLeavesTexturedModelFactory.get(card.block) })
    card.item.registerModelGeneration(Model("block/charged_" * card.identifier))
    card.block.registerCutoutRenderLayer()
    card.block.registerFoliageColorProvider()
    card.item.registerRedirectColorProvider()

    // 性質
    card.block.registerFlammable(30, 30)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.LEAVES }
    card.item.registerItemTagGeneration { ItemTags.LEAVES }
    card.block.registerBlockTagGeneration { BlockTags.HOE_MINEABLE }

}

context(ModContext)
private fun initLogHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    DataGenerationEvents.onGenerateBlockStateModel {
        it.registerLog(card.block).log(card.block)
    }

    // 性質
    card.block.registerFlammable(5, 5)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.OVERWORLD_NATURAL_LOGS }
    card.block.registerBlockTagGeneration { BlockTags.LOGS_THAT_BURN }
    card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS }
    card.item.registerItemTagGeneration { ItemTags.LOGS_THAT_BURN }

}

context(ModContext)
private fun initHorizontalFacingLogHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
    card.block.registerModelGeneration {
        Models.ORIENTABLE.with(
            TextureKey.TOP to "block/" * HaimeviskaBlockCard.LOG.block.getIdentifier() * "_top",
            TextureKey.SIDE to "block/" * HaimeviskaBlockCard.LOG.block.getIdentifier(),
            TextureKey.FRONT to "block/" * it.getIdentifier(),
        )
    }

    // 性質
    card.block.registerFlammable(5, 5)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.OVERWORLD_NATURAL_LOGS }
    card.block.registerBlockTagGeneration { BlockTags.LOGS_THAT_BURN }
    card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS }
    card.item.registerItemTagGeneration { ItemTags.LOGS_THAT_BURN }

}

context(ModContext)
private fun initPlanksHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerSingletonBlockStateGeneration()
    card.block.registerModelGeneration {
        Models.CUBE_ALL.with(
            TextureKey.ALL to "block/" * it.getIdentifier(),
        )
    }

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.PLANKS }
    card.item.registerItemTagGeneration { ItemTags.PLANKS }

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


val HAIMEVISKA_LOGS: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, MirageFairy2024.identifier("haimeviska_logs"))

context(ModContext)
fun initHaimeviskaBlocks() {

    HaimeviskaBlockCard.entries.forEach { card ->

        // 登録
        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        // カテゴリ
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // テキスト
        card.block.enJa(EnJa(card.configuration.enName, card.configuration.jaName))
        card.item.registerPoem(card.configuration.poemList)
        card.item.registerPoemGeneration(card.configuration.poemList)

        card.initializer(this@ModContext, card)
    }

    // ドロップ
    HaimeviskaBlockCard.LEAVES.block.registerLootTableGeneration {
        it.leavesDrops(HaimeviskaBlockCard.LEAVES.block, HaimeviskaBlockCard.SAPLING.block, *BlockLootTableGenerator.SAPLING_DROP_CHANCE)
    }
    HaimeviskaBlockCard.LOG.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.INCISED_LOG.block.registerLootTableGeneration { provider ->
        LootTable(
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.INCISED_LOG.item)) {
                conditionally(BlockLootTableGenerator.WITH_SILK_TOUCH)
            },
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.LOG.item)) {
                conditionally(BlockLootTableGenerator.WITHOUT_SILK_TOUCH)
            },
        ) {
            provider.applyExplosionDecay(HaimeviskaBlockCard.INCISED_LOG.block, this)
        }
    }
    HaimeviskaBlockCard.DRIPPING_LOG.block.registerLootTableGeneration { provider ->
        LootTable(
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.DRIPPING_LOG.item)) {
                conditionally(BlockLootTableGenerator.WITH_SILK_TOUCH)
            },
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.LOG.item)) {
                conditionally(BlockLootTableGenerator.WITHOUT_SILK_TOUCH)
            },
            LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_SAP.item) {
                apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.BLOCK_FORTUNE))
            }) {
                conditionally(BlockLootTableGenerator.WITHOUT_SILK_TOUCH)
            },
            LootPool(ItemLootPoolEntry(MaterialCard.HAIMEVISKA_ROSIN.item) {
                apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))
            }) {
                conditionally(BlockLootTableGenerator.WITHOUT_SILK_TOUCH)
                conditionally(RandomChanceLootCondition.builder(0.01F))
            },
        ) {
            provider.applyExplosionDecay(HaimeviskaBlockCard.DRIPPING_LOG.block, this)
        }
    }
    HaimeviskaBlockCard.HOLLOW_LOG.block.registerLootTableGeneration { provider ->
        LootTable(
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.HOLLOW_LOG.item)) {
                conditionally(BlockLootTableGenerator.WITH_SILK_TOUCH)
            },
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.LOG.item)) {
                conditionally(BlockLootTableGenerator.WITHOUT_SILK_TOUCH)
            },
            LootPool(ItemLootPoolEntry(MaterialCard.FRACTAL_WISP.item) {
                apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.BLOCK_FORTUNE))
            }) {
                conditionally(BlockLootTableGenerator.WITHOUT_SILK_TOUCH)
            },
        ) {
            provider.applyExplosionDecay(HaimeviskaBlockCard.HOLLOW_LOG.block, this)
        }
    }
    HaimeviskaBlockCard.PLANKS.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.SAPLING.block.registerDefaultLootTableGeneration()

    // レシピ
    HaimeviskaBlockCard.LEAVES.item.registerComposterInput(0.3F)
    HaimeviskaBlockCard.SAPLING.item.registerComposterInput(0.3F)
    registerShapelessRecipeGeneration(HaimeviskaBlockCard.PLANKS.item, 4) {
        input(HaimeviskaBlockCard.LOG.item)
    } on HaimeviskaBlockCard.LOG.item from HaimeviskaBlockCard.LOG.item
    HaimeviskaBlockCard.DRIPPING_LOG.item.registerHarvestNotation(MaterialCard.HAIMEVISKA_SAP.item, MaterialCard.HAIMEVISKA_ROSIN.item)
    HaimeviskaBlockCard.HOLLOW_LOG.item.registerHarvestNotation(MaterialCard.FRACTAL_WISP.item)

}


class HaimeviskaLeavesBlock(settings: Settings) : LeavesBlock(settings) {
    companion object {
        val CHARGED: BooleanProperty = BooleanProperty.of("charged")
    }

    init {
        defaultBlockState = defaultBlockState.with(CHARGED, true)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(CHARGED)
    }

    override fun hasRandomTicks(state: BlockState) = super.hasRandomTicks(state) || !state[CHARGED]

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        super.randomTick(state, world, pos, random)
        if (!state[CHARGED]) {
            if (random.randomBoolean(15, world.getLightLevel(pos))) {
                world.setBlockState(pos, state.with(CHARGED, true), Block.NOTIFY_LISTENERS)
            }
        }
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        super.randomDisplayTick(state, world, pos, random)
        if (random.nextInt(20) == 0) {
            val blockPos = pos.down()
            if (!isFaceFullSquare(world.getBlockState(blockPos).getCollisionShape(world, blockPos), Direction.UP)) {
                ParticleUtil.spawnParticle(world, pos, random, ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType)
            }
        }
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class HaimeviskaLogBlock(settings: Settings) : PillarBlock(settings) {
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (state.get(AXIS) != Direction.Axis.Y) @Suppress("DEPRECATION") return super.onUse(state, world, pos, player, hand, hit) // 縦方向でなければスルー
        val toolItemStack = player.getStackInHand(hand)
        if (!toolItemStack.isIn(ItemTags.SWORDS)) @Suppress("DEPRECATION") return super.onUse(state, world, pos, player, hand, hit) // 剣でなければスルー
        if (world.isClientSide) return ActionResult.SUCCESS
        val direction = if (hit.side.axis === Direction.Axis.Y) player.horizontalFacing.opposite else hit.side

        // 加工
        toolItemStack.damage(1, player) { it.sendToolBreakStatus(hand) }
        world.setBlockState(pos, HaimeviskaBlockCard.INCISED_LOG.block.defaultBlockState.with(HorizontalFacingBlock.FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)
        player.incrementStat(Stats.USED.getOrCreateStat(toolItemStack.item))

        // エフェクト
        world.playSound(null, pos, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F)

        return ActionResult.CONSUME
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class IncisedHaimeviskaLogBlock(settings: Settings) : SimpleHorizontalFacingBlock(settings) {
    override fun hasRandomTicks(state: BlockState) = true
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (random.nextInt(100) == 0) {
            world.setBlockState(pos, HaimeviskaBlockCard.DRIPPING_LOG.block.defaultBlockState.with(FACING, state.get(FACING)), Block.UPDATE_ALL)
        }
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class DrippingHaimeviskaLogBlock(settings: Settings) : SimpleHorizontalFacingBlock(settings) {
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (world.isClientSide) return ActionResult.SUCCESS
        val toolItemStack = player.getStackInHand(hand)
        val direction = state.get(FACING)

        // 消費
        world.setBlockState(pos, HaimeviskaBlockCard.INCISED_LOG.block.defaultBlockState.with(FACING, direction), Block.UPDATE_ALL or Block.UPDATE_IMMEDIATE)

        fun drop(item: Item, count: Double) {
            val actualCount = world.random.randomInt(count) atMost item.maxStackSize
            if (actualCount <= 0) return
            val itemStack = item.createItemStack(actualCount)
            val itemEntity = ItemEntity(world, pos.x + 0.5 + direction.offsetX * 0.65, pos.y + 0.1, pos.z + 0.5 + direction.offsetZ * 0.65, itemStack)
            itemEntity.setVelocity(0.05 * direction.offsetX + world.random.nextDouble() * 0.02, 0.05, 0.05 * direction.offsetZ + world.random.nextDouble() * 0.02)
            world.spawnEntity(itemEntity)
        }

        // 生産
        val fortune = EnchantmentHelper.getLevel(Enchantments.BLOCK_FORTUNE, toolItemStack)
        drop(MaterialCard.HAIMEVISKA_SAP.item, 1.0 + 0.25 * fortune) // ハイメヴィスカの樹液
        drop(MaterialCard.HAIMEVISKA_ROSIN.item, 0.03 + 0.01 * fortune) // 妖精の木の涙

        // エフェクト
        world.playSound(null, pos, SoundEvents.SLIME_JUMP, SoundCategory.BLOCKS, 0.75F, 1.0F + 0.5F * world.random.nextFloat())

        return ActionResult.CONSUME
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (random.nextFloat() >= 0.2F) return

        val direction = state.get(FACING)
        val destBlockPos = pos.offset(direction)
        val destBlockState = world.getBlockState(destBlockPos)
        val destShape = destBlockState.getCollisionShape(world, destBlockPos)
        val hasSpace = when (direction) {
            Direction.NORTH -> destShape.getMax(Direction.Axis.Z) < 1.0
            Direction.SOUTH -> destShape.getMin(Direction.Axis.Z) > 0.0
            Direction.WEST -> destShape.getMax(Direction.Axis.X) < 1.0
            Direction.EAST -> destShape.getMin(Direction.Axis.X) > 0.0
            else -> throw IllegalStateException()
        }
        if (!(hasSpace || !destBlockState.isFullCube(world, destBlockPos))) return

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

class HaimeviskaSaplingGenerator : SaplingGenerator() {
    override fun getTreeFeature(random: Random, bees: Boolean) = HAIMEVISKA_CONFIGURED_FEATURE_KEY
}
