package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
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
import miragefairy2024.util.CompletableRegistration
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
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
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
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
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BooleanProperty
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
        ).let { HaimeviskaBlockCard(it, { HollowHaimeviskaLogBlock(createSpecialLogSettings()) }, ::initHorizontalFacingLogHaimeviskaBlock) }
        val PLANKS = Configuration(
            "haimeviska_planks", "Haimeviska Planks", "ハイメヴィスカの板材",
            PoemList(1).poem("Flexible and friendly, good for interior", "考える、壁。"),
        ).let { HaimeviskaBlockCard(it, { Block(createPlankSettings()) }, ::initPlanksHaimeviskaBlock) }
        val SAPLING = Configuration(
            "haimeviska_sapling", "Haimeviska Sapling", "ハイメヴィスカの苗木",
            PoemList(1).poem("Assembling molecules with Ergs", "第二の葉緑体。"),
        ).let { HaimeviskaBlockCard(it, { SaplingBlock(createTreeGrower(MirageFairy2024.identifier("haimeviska_sapling")), createSaplingSettings()) }, ::initSaplingHaimeviskaBlock) }

        val entries = listOf(LEAVES, LOG, INCISED_LOG, DRIPPING_LOG, HOLLOW_LOG, PLANKS, SAPLING)
    }

    class Configuration(val path: String, val enName: String, val jaName: String, val poemList: PoemList)

    val identifier = MirageFairy2024.identifier(configuration.path)
    val block = blockCreator()
    val item = CompletableRegistration(BuiltInRegistries.ITEM, identifier) { BlockItem(block, Item.Properties()) }
}

private fun createLeavesSettings() = AbstractBlock.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(BlockSoundGroup.GRASS).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(PistonBehavior.DESTROY).isRedstoneConductor(Blocks::never)
private fun createBaseWoodSetting() = AbstractBlock.Properties.of().instrument(Instrument.BASS).sound(BlockSoundGroup.WOOD).ignitedByLava()
private fun createLogSettings() = createBaseWoodSetting().strength(2.0F).mapColor { if (it.getValue(PillarBlock.AXIS) === Direction.Axis.Y) MapColor.RAW_IRON else MapColor.TERRACOTTA_ORANGE }
private fun createSpecialLogSettings() = createBaseWoodSetting().strength(2.0F).mapColor(MapColor.RAW_IRON)
private fun createPlankSettings() = createBaseWoodSetting().strength(2.0F, 3.0F).mapColor(MapColor.RAW_IRON)
private fun createSaplingSettings() = AbstractBlock.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(BlockSoundGroup.GRASS).pushReaction(PistonBehavior.DESTROY)

private fun createTreeGrower(identifier: ResourceLocation) = TreeGrower(identifier.string, Optional.empty(), Optional.of(HAIMEVISKA_CONFIGURED_FEATURE_KEY), Optional.empty())

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
    card.item().registerItemTagGeneration { ItemTags.LEAVES }
    card.block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_HOE }

}

context(ModContext)
private fun initLogHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    DataGenerationEvents.onGenerateBlockStateModel {
        it.woodProvider(card.block).logWithHorizontal(card.block)
    }

    // 性質
    card.block.registerFlammable(5, 5)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.OVERWORLD_NATURAL_LOGS }
    card.block.registerBlockTagGeneration { BlockTags.LOGS_THAT_BURN }
    card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS }
    card.item().registerItemTagGeneration { ItemTags.LOGS_THAT_BURN }

}

context(ModContext)
private fun initHorizontalFacingLogHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
    card.block.registerModelGeneration {
        Models.CUBE_ORIENTABLE.with(
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
    card.item().registerItemTagGeneration { ItemTags.LOGS_THAT_BURN }

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
    card.item().registerItemTagGeneration { ItemTags.PLANKS }

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
    card.item().registerItemTagGeneration { ItemTags.SAPLINGS }

}


val HAIMEVISKA_LOGS: TagKey<Block> = TagKey.create(Registries.BLOCK, MirageFairy2024.identifier("haimeviska_logs"))

context(ModContext)
fun initHaimeviskaBlocks() {

    BuiltInRegistries.BLOCK_TYPE.register(MirageFairy2024.identifier("haimeviska_leaves")) { HaimeviskaLeavesBlock.CODEC }
    BuiltInRegistries.BLOCK_TYPE.register(MirageFairy2024.identifier("haimeviska_log")) { HaimeviskaLogBlock.CODEC }
    BuiltInRegistries.BLOCK_TYPE.register(MirageFairy2024.identifier("incised_haimeviska_log")) { IncisedHaimeviskaLogBlock.CODEC }
    BuiltInRegistries.BLOCK_TYPE.register(MirageFairy2024.identifier("dripping_haimeviska_log")) { DrippingHaimeviskaLogBlock.CODEC }
    BuiltInRegistries.BLOCK_TYPE.register(MirageFairy2024.identifier("hollow_haimeviska_log")) { HollowHaimeviskaLogBlock.CODEC }


    HaimeviskaBlockCard.entries.forEach { card ->

        // 登録
        BuiltInRegistries.BLOCK.register(card.identifier) { card.block }
        card.item.register()

        // カテゴリ
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        // テキスト
        card.block.enJa(EnJa(card.configuration.enName, card.configuration.jaName))
        card.item.registerPoem(card.configuration.poemList)
        card.item.registerPoemGeneration(card.configuration.poemList)

        card.initializer(this@ModContext, card)
    }

    // ドロップ
    HaimeviskaBlockCard.LEAVES.block.registerLootTableGeneration { it, _ ->
        it.createLeavesDrops(HaimeviskaBlockCard.LEAVES.block, HaimeviskaBlockCard.SAPLING.block, 0.05F, 0.0625F, 0.083333336F, 0.1F)
    }
    HaimeviskaBlockCard.LOG.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.INCISED_LOG.block.registerLootTableGeneration { provider, _ ->
        LootTable(
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.INCISED_LOG.item())) {
                `when`(provider.hasSilkTouch())
            },
            LootPool(ItemLootPoolEntry(HaimeviskaBlockCard.LOG.item())) {
                `when`(provider.doesNotHaveSilkTouch())
            },
        ) {
            provider.applyExplosionDecay(HaimeviskaBlockCard.INCISED_LOG.block, this)
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
            provider.applyExplosionDecay(HaimeviskaBlockCard.DRIPPING_LOG.block, this)
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
            provider.applyExplosionDecay(HaimeviskaBlockCard.HOLLOW_LOG.block, this)
        }
    }
    HaimeviskaBlockCard.PLANKS.block.registerDefaultLootTableGeneration()
    HaimeviskaBlockCard.SAPLING.block.registerDefaultLootTableGeneration()

    // レシピ
    HaimeviskaBlockCard.LEAVES.item.registerComposterInput(0.3F)
    HaimeviskaBlockCard.SAPLING.item.registerComposterInput(0.3F)
    registerShapelessRecipeGeneration(HaimeviskaBlockCard.PLANKS.item, 4) {
        requires(HaimeviskaBlockCard.LOG.item())
    } on HaimeviskaBlockCard.LOG.item from HaimeviskaBlockCard.LOG.item
    HaimeviskaBlockCard.DRIPPING_LOG.item().registerHarvestNotation(MaterialCard.HAIMEVISKA_SAP.item(), MaterialCard.HAIMEVISKA_ROSIN.item())
    HaimeviskaBlockCard.HOLLOW_LOG.item().registerHarvestNotation(MaterialCard.FRACTAL_WISP.item())

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
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block.defaultBlockState().setValue(HorizontalFacingBlock.FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)
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
            world.setBlock(pos, HaimeviskaBlockCard.DRIPPING_LOG.block.defaultBlockState().setValue(FACING, state.getValue(FACING)), Block.UPDATE_ALL)
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
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block.defaultBlockState().setValue(FACING, direction), Block.UPDATE_ALL or Block.UPDATE_IMMEDIATE)

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
