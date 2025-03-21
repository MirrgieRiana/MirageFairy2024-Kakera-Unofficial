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
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.registry.tag.BlockTags
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

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

        block.registerBlockTagGeneration { BlockTags.PICKAXE_MINEABLE }

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

        override fun asString() = string
    }


    // BlockState

    init {
        defaultState = defaultState.with(VERTICAL_FACING, VerticalFacing.SIDE)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(VERTICAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val verticalFacing = when (ctx.side) {
            Direction.UP -> VerticalFacing.DOWN
            Direction.DOWN -> VerticalFacing.UP
            else -> VerticalFacing.SIDE
        }
        val facing = if (verticalFacing == VerticalFacing.SIDE) ctx.side.opposite else ctx.horizontalPlayerFacing
        return defaultState.with(VERTICAL_FACING, verticalFacing).with(FACING, facing)
    }

}

abstract class FairyLogisticsBlockEntity<E : FairyLogisticsBlockEntity<E>>(card: FairyLogisticsCard<*, E, *>, pos: BlockPos, state: BlockState) : MachineBlockEntity<E>(card, pos, state) {

    // Inventory

    override fun getActualSide(side: Direction) = HorizontalFacingMachineBlock.getActualSide(cachedState, side)

    fun getTarget(): Pair<Inventory, Direction>? {
        fun f(blockPos: BlockPos, side: Direction): Pair<Inventory, Direction>? {
            val world = world ?: return null
            val blockEntity = world.getBlockEntity(blockPos) ?: return null
            if (blockEntity !is Inventory) return null
            return Pair(blockEntity, side)
        }
        return when (cachedState[FairyLogisticsBlock.VERTICAL_FACING]) {
            FairyLogisticsBlock.VerticalFacing.UP -> f(pos.up(), Direction.DOWN)
            FairyLogisticsBlock.VerticalFacing.SIDE -> when (cachedState[HorizontalFacingBlock.FACING]) {
                Direction.NORTH -> f(pos.north(), Direction.SOUTH)
                Direction.SOUTH -> f(pos.south(), Direction.NORTH)
                Direction.WEST -> f(pos.west(), Direction.EAST)
                Direction.EAST -> f(pos.east(), Direction.WEST)
                else -> null
            }

            FairyLogisticsBlock.VerticalFacing.DOWN -> f(pos.down(), Direction.UP)
            else -> null
        }
    }

}

open class FairyLogisticsScreenHandler(card: FairyLogisticsCard<*, *, *>, arguments: Arguments) : MachineScreenHandler(card, arguments)
