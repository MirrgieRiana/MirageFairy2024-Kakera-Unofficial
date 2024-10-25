package miragefairy2024.mod.fairylogistics

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.RichMachineBlock
import miragefairy2024.lib.RichMachineBlockEntity
import miragefairy2024.lib.RichMachineScreenHandler
import miragefairy2024.lib.createScreenHandlerType
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.BlockStateVariantEntry
import miragefairy2024.util.BlockStateVariantRotation
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.getOrNull
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

abstract class FairyLogisticsNodeConfiguration<C : FairyLogisticsNodeCard<C, S, B, E, H>, S : FairyLogisticsNodeConfiguration<C, S, B, E, H>, B : FairyLogisticsNodeBlock<C>, E : FairyLogisticsNodeBlockEntity<C>, H : FairyLogisticsNodeScreenHandler<C>> {
    companion object {
        fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().strength(1.0F).pistonBehavior(PistonBehavior.DESTROY)
    }

    abstract val path: String
    abstract val name: EnJa
    abstract val tier: Int
    abstract val poem: EnJa
    abstract fun createBlock(card: C): B
    abstract fun createBlockEntity(card: C, blockPos: BlockPos, blockState: BlockState): E
    abstract val slots: List<Unit>
    abstract fun createScreenHandler(card: C, arguments: RichMachineScreenHandler.Arguments): H

    context(ModContext)
    open fun init(card: C) {

        card.block.register(Registries.BLOCK, card.identifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)
        card.screenHandlerType.register(Registries.SCREEN_HANDLER, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration {
            fun f(verticalFacing: FairyLogisticsNodeBlock.VerticalFacing, direction: Direction, suffix: String, y: BlockStateVariantRotation): BlockStateVariantEntry {
                return propertiesOf(
                    FairyLogisticsNodeBlock.VERTICAL_FACING with verticalFacing,
                    FairyLogisticsNodeBlock.FACING with direction,
                ) with BlockStateVariant(model = "block/" * card.block.getIdentifier() * suffix).with(y = y)
            }
            listOf(
                f(FairyLogisticsNodeBlock.VerticalFacing.UP, Direction.NORTH, "_up", BlockStateVariantRotation.R180),
                f(FairyLogisticsNodeBlock.VerticalFacing.UP, Direction.EAST, "_up", BlockStateVariantRotation.R270),
                f(FairyLogisticsNodeBlock.VerticalFacing.UP, Direction.SOUTH, "_up", BlockStateVariantRotation.R0),
                f(FairyLogisticsNodeBlock.VerticalFacing.UP, Direction.WEST, "_up", BlockStateVariantRotation.R90),
                f(FairyLogisticsNodeBlock.VerticalFacing.SIDE, Direction.NORTH, "", BlockStateVariantRotation.R180),
                f(FairyLogisticsNodeBlock.VerticalFacing.SIDE, Direction.EAST, "", BlockStateVariantRotation.R270),
                f(FairyLogisticsNodeBlock.VerticalFacing.SIDE, Direction.SOUTH, "", BlockStateVariantRotation.R0),
                f(FairyLogisticsNodeBlock.VerticalFacing.SIDE, Direction.WEST, "", BlockStateVariantRotation.R90),
                f(FairyLogisticsNodeBlock.VerticalFacing.DOWN, Direction.NORTH, "_down", BlockStateVariantRotation.R180),
                f(FairyLogisticsNodeBlock.VerticalFacing.DOWN, Direction.EAST, "_down", BlockStateVariantRotation.R270),
                f(FairyLogisticsNodeBlock.VerticalFacing.DOWN, Direction.SOUTH, "_down", BlockStateVariantRotation.R0),
                f(FairyLogisticsNodeBlock.VerticalFacing.DOWN, Direction.WEST, "_down", BlockStateVariantRotation.R90),
            )
        }
        card.block.registerCutoutRenderLayer()
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        card.block.enJa(name)
        val poemList = PoemList(tier).poem(poem)
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        card.block.registerBlockTagGeneration { BlockTags.PICKAXE_MINEABLE }

        card.block.registerDefaultLootTableGeneration()

    }
}

abstract class FairyLogisticsNodeCard<C : FairyLogisticsNodeCard<C, S, B, E, H>, S : FairyLogisticsNodeConfiguration<C, S, B, E, H>, B : FairyLogisticsNodeBlock<C>, E : FairyLogisticsNodeBlockEntity<C>, H : FairyLogisticsNodeScreenHandler<C>>(val configuration: S) {
    abstract val self: C
    val identifier = MirageFairy2024.identifier(configuration.path)
    val block by lazy { configuration.createBlock(self) }
    val blockEntityType = BlockEntityType({ pos, state -> configuration.createBlockEntity(self, pos, state) }, setOf(block), null)
    val item by lazy { BlockItem(block, Item.Settings()) }
    val screenHandlerConfiguration: RichMachineScreenHandler.Configuration = object : RichMachineScreenHandler.Configuration {
        override val type get() = screenHandlerType
        override val width = 5 + 18 * 9 + 5 // TODO
        override val height = 200 // TODO
    }
    val screenHandlerType = screenHandlerConfiguration.createScreenHandlerType { arguments, _ -> createScreenHandler(arguments) }
    fun createScreenHandler(arguments: RichMachineScreenHandler.Arguments) = configuration.createScreenHandler(self, arguments)

    context(ModContext)
    fun init() = configuration.init(self)
}

open class FairyLogisticsNodeBlock<C : FairyLogisticsNodeCard<C, *, *, *, *>>(val cardGetter: () -> C, settings: Settings) :
    RichMachineBlock(object : Arguments {
        override val settings = settings
        override val blockEntityType = cardGetter().blockEntityType
    }) {
    companion object {
        val VERTICAL_FACING: EnumProperty<VerticalFacing> = EnumProperty.of("vertical_facing", VerticalFacing::class.java)
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }

    enum class VerticalFacing(val string: String, val id: Int) : StringIdentifiable {
        UP("up", 0),
        SIDE("side", 1),
        DOWN("down", 2),
        ;

        override fun asString() = string
    }


    // BlockState

    init {
        defaultState = defaultState.with(VERTICAL_FACING, VerticalFacing.SIDE).with(FACING, Direction.NORTH)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(VERTICAL_FACING)
        builder.add(FACING)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState = state.with(FACING, rotation.rotate(state[FACING]))

    @Suppress("OVERRIDE_DEPRECATION")
    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState = state.rotate(mirror.getRotation(state[FACING]))

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val verticalFacing = when (ctx.side) {
            Direction.UP -> VerticalFacing.DOWN
            Direction.DOWN -> VerticalFacing.UP
            else -> VerticalFacing.SIDE
        }
        val facing = if (verticalFacing == VerticalFacing.SIDE) ctx.side.opposite else ctx.horizontalPlayerFacing
        return defaultState.with(VERTICAL_FACING, verticalFacing).with(FACING, facing)
    }


    // BlockEntity

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = cardGetter().configuration.createBlockEntity(cardGetter(), pos, state)

}

abstract class FairyLogisticsNodeBlockEntity<C : FairyLogisticsNodeCard<C, *, *, *, *>>(val card: C, pos: BlockPos, state: BlockState) :
    RichMachineBlockEntity(Arguments(object : Configuration {
        override val type = card.blockEntityType
    }, pos, state)) {

    override fun getHorizontalFacing() = cachedState.getOrNull(HorizontalFacingBlock.FACING) ?: Direction.NORTH

    override fun getContainerName(): Text = card.block.name

    override fun createAnimationConfigurations(): List<AnimationConfiguration> = TODO()

    override fun createProperties() = TODO()

    override fun createScreenHandler(screenHandlerArguments: RichMachineScreenHandler.Arguments) = card.createScreenHandler(screenHandlerArguments)

}

open class FairyLogisticsNodeScreenHandler<C : FairyLogisticsNodeCard<C, *, *, *, *>>(val card: C, arguments: Arguments) :
    RichMachineScreenHandler(arguments) {

    override fun canUse(player: PlayerEntity?) = canUse(arguments.context, player, card.block)

}
