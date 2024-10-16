package miragefairy2024.mod.fairylogistics

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.lib.SimpleMachineScreenHandler
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.BlockStateVariantEntry
import miragefairy2024.util.BlockStateVariantRotation
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
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
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.ScreenHandler
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class FairyLogisticsBlockConfiguration {
    abstract val path: String
    abstract val name: EnJa
    abstract val tier: Int
    abstract val poem: EnJa
    abstract fun createBlock(cardGetter: () -> FairyLogisticsBlockCard): FairyLogisticsBlock
    abstract fun createBlockEntity(card: FairyLogisticsBlockCard, blockPos: BlockPos, blockState: BlockState): FairyLogisticsBlockEntity
    abstract val slots: List<Unit>
    abstract fun createScreenHandler(card: FairyLogisticsBlockCard, syncId: Int, playerInventory: PlayerInventory): ScreenHandler

    context(ModContext)
    open fun init(card: FairyLogisticsBlockCard) {

        card.block.register(Registries.BLOCK, card.identifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration {
            fun f(verticalFacing: FairyLogisticsBlock.VerticalFacing, direction: Direction, suffix: String, y: BlockStateVariantRotation): BlockStateVariantEntry {
                return propertiesOf(
                    FairyLogisticsBlock.VERTICAL_FACING with verticalFacing,
                    FairyLogisticsBlock.FACING with direction,
                ) with BlockStateVariant(model = "block/" * card.block.getIdentifier() * suffix).with(y = y)
            }
            listOf(
                f(FairyLogisticsBlock.VerticalFacing.UP, Direction.NORTH, "_up", BlockStateVariantRotation.R180),
                f(FairyLogisticsBlock.VerticalFacing.UP, Direction.EAST, "_up", BlockStateVariantRotation.R270),
                f(FairyLogisticsBlock.VerticalFacing.UP, Direction.SOUTH, "_up", BlockStateVariantRotation.R0),
                f(FairyLogisticsBlock.VerticalFacing.UP, Direction.WEST, "_up", BlockStateVariantRotation.R90),
                f(FairyLogisticsBlock.VerticalFacing.SIDE, Direction.NORTH, "", BlockStateVariantRotation.R180),
                f(FairyLogisticsBlock.VerticalFacing.SIDE, Direction.EAST, "", BlockStateVariantRotation.R270),
                f(FairyLogisticsBlock.VerticalFacing.SIDE, Direction.SOUTH, "", BlockStateVariantRotation.R0),
                f(FairyLogisticsBlock.VerticalFacing.SIDE, Direction.WEST, "", BlockStateVariantRotation.R90),
                f(FairyLogisticsBlock.VerticalFacing.DOWN, Direction.NORTH, "_down", BlockStateVariantRotation.R180),
                f(FairyLogisticsBlock.VerticalFacing.DOWN, Direction.EAST, "_down", BlockStateVariantRotation.R270),
                f(FairyLogisticsBlock.VerticalFacing.DOWN, Direction.SOUTH, "_down", BlockStateVariantRotation.R0),
                f(FairyLogisticsBlock.VerticalFacing.DOWN, Direction.WEST, "_down", BlockStateVariantRotation.R90),
            )
        }
        card.block.registerCutoutRenderLayer()
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        card.block.enJa(card.configuration.name)
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.block.registerBlockTagGeneration { BlockTags.PICKAXE_MINEABLE }

        card.block.registerDefaultLootTableGeneration()

    }
}

fun createFairyLogisticsBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().strength(1.0F).pistonBehavior(PistonBehavior.DESTROY)

open class FairyLogisticsBlockCard(val configuration: FairyLogisticsBlockConfiguration) {
    val identifier = MirageFairy2024.identifier(configuration.path)
    val block = configuration.createBlock { this }
    val blockEntityType = BlockEntityType({ pos, state -> configuration.createBlockEntity(this, pos, state) }, setOf(block), null)
    val item = BlockItem(block, Item.Settings())
    val poemList = PoemList(configuration.tier).poem(configuration.poem)
    val screenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
        configuration.createScreenHandler(this, syncId, playerInventory)
    }
}

open class FairyLogisticsBlock(private val cardGetter: () -> FairyLogisticsBlockCard, settings: Settings) : Block(settings), BlockEntityProvider {
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

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            run {
                val blockEntity = world.getBlockEntity(pos) as? FairyLogisticsBlockEntity ?: return@run
                blockEntity.dropItems()
            }
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }

}

abstract class FairyLogisticsBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : LockableContainerBlockEntity(type, pos, state), RenderingProxyBlockEntity {

    fun dropItems() {
        // TODO
    }

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        // TODO
        val facing = cachedState[FairyLogisticsBlock.FACING]
        renderingProxy.stack {
            renderingProxy.translate(0.5, 0.5, 0.5)
            renderingProxy.rotateY(-((facing.horizontal + 2) * 90) / 180F * Math.PI.toFloat())
            renderingProxy.rotateX(0F)
            renderingProxy.renderItemStack(Items.IRON_INGOT.createItemStack())
        }
    }

}

open class FairyLogisticsScreenHandler(private val card: FairyLogisticsBlockCard, arguments: Arguments<Configuration>) : SimpleMachineScreenHandler<SimpleMachineScreenHandler.Configuration>(arguments) {

    override fun canUse(player: PlayerEntity?) = canUse(arguments.context, player, card.block)

}
