package miragefairy2024.mod.fairylogistics

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.HorizontalFacingMachineBlock
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
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
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.material.PushReaction as PistonBehavior
import net.minecraft.world.Container as Inventory
import net.minecraft.world.item.context.BlockPlaceContext as ItemPlacementContext
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.StateDefinition as StateManager
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.util.StringRepresentable as StringIdentifiable
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

abstract class FairyLogisticsCard<B : FairyLogisticsBlock, E : FairyLogisticsBlockEntity<E>, H : FairyLogisticsScreenHandler> : MachineCard<B, E, H>() {

    // Specification

    abstract fun getPath(): String
    override fun createIdentifier() = MirageFairy2024.identifier(getPath())

    abstract val tier: Int
    abstract val name: EnJa
    abstract val poem: EnJa
    abstract val description: EnJa


    // Block

    override fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().strength(1.0F).pistonBehavior(PistonBehavior.DESTROY)


    context(ModContext)
    override fun init() {
        super.init()

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        block.registerVariantsBlockStateGeneration {
            fun f(verticalFacing: FairyLogisticsBlock.VerticalFacing, direction: Direction, suffix: String, y: BlockStateVariantRotation): BlockStateVariantEntry {
                return propertiesOf(
                    FairyLogisticsBlock.VERTICAL_FACING with verticalFacing,
                    HorizontalFacingBlock.FACING with direction,
                ) with BlockStateVariant(model = "block/" * block.getIdentifier() * suffix).with(y = y)
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
        block.registerCutoutRenderLayer()
        blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        block.enJa(name)
        val poemList = PoemList(tier).poem(poem).description(description)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_PICKAXE }

        block.registerDefaultLootTableGeneration()

    }
}

open class FairyLogisticsBlock(card: FairyLogisticsCard<*, *, *>) : HorizontalFacingMachineBlock(card) {
    companion object {
        val VERTICAL_FACING: EnumProperty<VerticalFacing> = EnumProperty.of("vertical_facing", VerticalFacing::class.java)
    }

    enum class VerticalFacing(val string: String, val id: Int) : StringIdentifiable {
        UP("up", 0),
        SIDE("side", 1),
        DOWN("down", 2),
        ;

        override fun getSerializedName() = string
    }


    // BlockState

    init {
        registerDefaultState(defaultBlockState().setValue(VERTICAL_FACING, VerticalFacing.SIDE))
    }

    override fun createBlockStateDefinition(builder: StateManager.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(VERTICAL_FACING)
    }

    override fun getStateForPlacement(ctx: ItemPlacementContext): BlockState {
        val verticalFacing = when (ctx.clickedFace) {
            Direction.UP -> VerticalFacing.DOWN
            Direction.DOWN -> VerticalFacing.UP
            else -> VerticalFacing.SIDE
        }
        val facing = if (verticalFacing == VerticalFacing.SIDE) ctx.clickedFace.opposite else ctx.horizontalDirection
        return defaultBlockState().setValue(VERTICAL_FACING, verticalFacing).setValue(FACING, facing)
    }

}

abstract class FairyLogisticsBlockEntity<E : FairyLogisticsBlockEntity<E>>(card: FairyLogisticsCard<*, E, *>, pos: BlockPos, state: BlockState) : MachineBlockEntity<E>(card, pos, state) {

    // Inventory

    override fun getActualSide(side: Direction) = HorizontalFacingMachineBlock.getActualSide(blockState, side)

    fun getTarget(): Pair<Inventory, Direction>? {
        fun f(blockPos: BlockPos, side: Direction): Pair<Inventory, Direction>? {
            val world = level ?: return null
            val blockEntity = world.getBlockEntity(blockPos) ?: return null
            if (blockEntity !is Inventory) return null
            return Pair(blockEntity, side)
        }
        return when (blockState[FairyLogisticsBlock.VERTICAL_FACING]) {
            FairyLogisticsBlock.VerticalFacing.UP -> f(worldPosition.up(), Direction.DOWN)
            FairyLogisticsBlock.VerticalFacing.SIDE -> when (blockState[HorizontalFacingBlock.FACING]) {
                Direction.NORTH -> f(worldPosition.north(), Direction.SOUTH)
                Direction.SOUTH -> f(worldPosition.south(), Direction.NORTH)
                Direction.WEST -> f(worldPosition.west(), Direction.EAST)
                Direction.EAST -> f(worldPosition.east(), Direction.WEST)
                else -> null
            }

            FairyLogisticsBlock.VerticalFacing.DOWN -> f(worldPosition.below(), Direction.UP)
            else -> null
        }
    }

}

open class FairyLogisticsScreenHandler(card: FairyLogisticsCard<*, *, *>, arguments: Arguments) : MachineScreenHandler(card, arguments)
