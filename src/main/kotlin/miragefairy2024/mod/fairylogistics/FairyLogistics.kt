package miragefairy2024.mod.fairylogistics

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.HorizontalFacingMachineBlock
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerSingletonBlockStateGeneration
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
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

    override fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create()


    context(ModContext)
    override fun init() {
        super.init()

        block.registerSingletonBlockStateGeneration()
        blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        block.enJa(name)
        val poemList = PoemList(tier).poem(poem).description(description)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

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

    override fun getActualSide(side: Direction): Direction {
        return when (side) {
            Direction.UP, Direction.DOWN -> side

            else -> {
                val direction = cachedState.getOrNull(HorizontalFacingBlock.FACING) ?: Direction.NORTH
                Direction.fromHorizontal((direction.horizontal + side.horizontal) % 4)
            }
        }
    }

}

open class FairyLogisticsScreenHandler(card: FairyLogisticsCard<*, *, *>, arguements: Arguments) : MachineScreenHandler(card, arguements)
