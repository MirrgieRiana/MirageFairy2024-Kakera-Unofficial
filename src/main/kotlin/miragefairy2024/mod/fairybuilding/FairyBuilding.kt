package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.RichMachineBlock
import miragefairy2024.lib.RichMachineBlockEntity
import miragefairy2024.lib.RichMachineScreenHandler
import miragefairy2024.lib.createScreenHandlerType
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.getOrNull
import miragefairy2024.util.normal
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.withHorizontalRotation
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.enums.Instrument
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

abstract class FairyBuildingConfiguration<C : FairyBuildingCard<C, S, B, E, H>, S : FairyBuildingConfiguration<C, S, B, E, H>, B : FairyBuildingBlock<C>, E : FairyBuildingBlockEntity<C, E>, H : FairyBuildingScreenHandler<C>> {
    companion object {
        inline fun <C : FairyBuildingCard<C, *, *, E, *>, reified E : FairyBuildingBlockEntity<C, E>> BlockEntityAccessor(crossinline creator: (card: C, blockPos: BlockPos, blockState: BlockState) -> E) = object : BlockEntityAccessor<C, E> {
            override fun create(card: C, blockPos: BlockPos, blockState: BlockState) = creator(card, blockPos, blockState)
            override fun castOrThrow(blockEntity: BlockEntity?) = blockEntity as E
            override fun castOrNull(blockEntity: BlockEntity?) = blockEntity as? E
        }
    }


    abstract val path: String
    abstract val tier: Int
    abstract val name: EnJa
    abstract val poem: EnJa


    open fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().nonOpaque().strength(2.0F).instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.RAW_IRON_PINK)

    abstract fun createBlock(cardGetter: () -> C, settings: FabricBlockSettings): B


    abstract fun createBlockEntityAccessor(): BlockEntityAccessor<C, E>

    interface BlockEntityAccessor<C : FairyBuildingCard<C, *, *, E, *>, E : FairyBuildingBlockEntity<C, E>> {
        fun create(card: C, blockPos: BlockPos, blockState: BlockState): E
        fun castOrThrow(blockEntity: BlockEntity?): E
        fun castOrNull(blockEntity: BlockEntity?): E?
    }


    abstract fun createScreenHandler(card: C, arguments: RichMachineScreenHandler.Arguments): H


    abstract val guiWidth: Int
    abstract val guiHeight: Int


    open fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> = listOf()

    class FairyBuildingSlotConfiguration(
        override val x: Int,
        override val y: Int,
        override val shouldDropItem: Boolean = true,
        val insertDirections: Set<Direction> = setOf(),
        val extractDirections: Set<Direction> = setOf(),
        val animation: SlotAnimationConfiguration? = null,
        val toolTipGetter: (() -> List<Text>)? = null,
        private val filter: (ItemStack) -> Boolean = { true },
    ) : RichMachineScreenHandler.SlotConfiguration, RichMachineBlockEntity.SlotConfiguration {
        override fun filter(itemStack: ItemStack) = filter.invoke(itemStack)
        override fun canInsertTo(direction: Direction) = direction in insertDirections
        override fun canExtractFrom(direction: Direction) = direction in extractDirections
    }

    class SlotAnimationConfiguration(val motion: RichMachineBlockEntity.Motion, val positions: List<RichMachineBlockEntity.Position>)


    open fun createPropertyConfigurations(): List<FairyBuildingPropertyConfiguration<E>> = listOf()

    interface FairyBuildingPropertyConfiguration<in E : FairyBuildingBlockEntity<*, *>> : RichMachineScreenHandler.PropertyConfiguration {
        fun createProperty(blockEntity: E): RichMachineScreenHandler.Property
    }


    context(ModContext)
    open fun init(card: C) {

        card.block.register(Registries.BLOCK, card.identifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)
        card.screenHandlerType.register(Registries.SCREEN_HANDLER, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        card.block.registerCutoutRenderLayer()
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        card.block.enJa(name)
        val poemList = PoemList(tier).poem(poem)
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        card.block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }
        card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS }

        card.block.registerDefaultLootTableGeneration()

    }
}

abstract class FairyBuildingCard<C : FairyBuildingCard<C, S, B, E, H>, S : FairyBuildingConfiguration<C, S, B, E, H>, B : FairyBuildingBlock<C>, E : FairyBuildingBlockEntity<C, E>, H : FairyBuildingScreenHandler<C>>(val configuration: S) {
    abstract val self: C

    val identifier = MirageFairy2024.identifier(configuration.path)

    val block = configuration.createBlock({ self }, configuration.createBlockSettings())

    val blockEntityAccessor = configuration.createBlockEntityAccessor()
    val blockEntityType = BlockEntityType({ pos, state -> blockEntityAccessor.create(self, pos, state) }, setOf(block), null)
    fun createBlockEntity(pos: BlockPos, state: BlockState) = blockEntityAccessor.create(self, pos, state)

    val item = BlockItem(block, Item.Settings())

    val slotConfigurations = configuration.createSlotConfigurations()

    val propertyConfigurations = configuration.createPropertyConfigurations()

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"

    val screenHandlerConfiguration = object : RichMachineScreenHandler.Configuration {
        override val type = run { this@FairyBuildingCard }.screenHandlerType
        override val width = configuration.guiWidth
        override val height = configuration.guiHeight
        override val machineSlotConfigurations = this@FairyBuildingCard.slotConfigurations
        override val propertyConfigurations = this@FairyBuildingCard.propertyConfigurations
    }
    val screenHandlerType: ExtendedScreenHandlerType<H> = screenHandlerConfiguration.createScreenHandlerType { arguments, _ -> createScreenHandler(arguments) }
    fun createProperties(blockEntity: E) = propertyConfigurations.map { it.createProperty(blockEntity) }
    fun createScreenHandler(arguments: RichMachineScreenHandler.Arguments) = configuration.createScreenHandler(self, arguments)

    context(ModContext)
    fun init() = configuration.init(self)
}

open class FairyBuildingBlock<C : FairyBuildingCard<C, *, *, *, *>>(val cardGetter: () -> C, settings: Settings) :
    RichMachineBlock(object : Arguments {
        override val settings = settings
        override val blockEntityType = cardGetter().blockEntityType
    }) {
    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
        private val SHAPE = VoxelShapes.union(
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 0.1),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 0.1, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 0.1, 16.0, 16.0),
            createCuboidShape(0.0, 0.0, 15.9, 16.0, 16.0, 16.0),
            createCuboidShape(0.0, 15.9, 0.0, 16.0, 16.0, 16.0),
            createCuboidShape(15.9, 0.0, 0.0, 16.0, 16.0, 16.0),
        )
    }


    // Block State

    init {
        defaultState = defaultState.with(FACING, Direction.NORTH)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState = state.with(FACING, rotation.rotate(state[FACING]))

    @Suppress("OVERRIDE_DEPRECATION")
    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState = state.rotate(mirror.getRotation(state[FACING]))

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState = defaultState.with(FACING, ctx.horizontalPlayerFacing.opposite)


    // Block Entity

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = cardGetter().createBlockEntity(pos, state)


    // Status

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos) = 6

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE

}

abstract class FairyBuildingBlockEntity<C : FairyBuildingCard<C, *, *, E, *>, E : FairyBuildingBlockEntity<C, E>>(val card: C, pos: BlockPos, state: BlockState) :
    RichMachineBlockEntity(Arguments(object : Configuration {
        override val type = card.blockEntityType
        override val slotConfigurations = card.slotConfigurations
        override val screenHandlerConfiguration = card.screenHandlerConfiguration
    }, pos, state)) {

    abstract val self: E

    override fun getHorizontalFacing() = cachedState.getOrNull(HorizontalFacingBlock.FACING) ?: Direction.NORTH

    override fun getContainerName(): Text = card.block.name

    open val doMovePosition get() = false

    override fun createAnimationConfigurations(): List<AnimationConfiguration> = card.slotConfigurations.mapIndexedNotNull { slotIndex, slotConfiguration ->
        val slotAnimationConfiguration = slotConfiguration.animation ?: return@mapIndexedNotNull null
        object : AnimationConfiguration {
            override fun getItemStack() = getStack(slotIndex)
            override fun getMotion() = slotAnimationConfiguration.motion
            override val positions = slotAnimationConfiguration.positions
            override fun getSpeed() = if (doMovePosition) 1.0 else 0.0
            override val slotIndex = slotIndex
        }
    }

    override fun createProperties() = card.createProperties(self)

    override fun createScreenHandler(screenHandlerArguments: RichMachineScreenHandler.Arguments) = card.createScreenHandler(screenHandlerArguments)

}

open class FairyBuildingScreenHandler<C : FairyBuildingCard<C, *, *, *, *>>(val card: C, arguments: Arguments) :
    RichMachineScreenHandler(arguments) {

    override fun canUse(player: PlayerEntity) = canUse(arguments.context, player, card.block)

}
