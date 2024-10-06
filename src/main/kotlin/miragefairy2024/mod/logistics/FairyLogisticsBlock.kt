package miragefairy2024.mod.logistics

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
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
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.ShapeContext
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView


abstract class FairyLogisticsBlockConfiguration {
    abstract val path: String
    abstract val name: EnJa
    abstract val tier: Int
    abstract val poem: EnJa
    abstract fun createBlock(): FairyLogisticsBlock

    context(ModContext)
    open fun init(card: FairyLogisticsBlockCard) {

        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration {
            fun f(direction: Direction, suffix: String, y: BlockStateVariantRotation): BlockStateVariantEntry {
                return propertiesOf(HorizontalFacingBlock.FACING with direction) with BlockStateVariant(model = "block/" * card.block.getIdentifier() * suffix).with(y = y)
            }
            listOf(
                f(Direction.UP, "", BlockStateVariantRotation.R0),
                f(Direction.DOWN, "", BlockStateVariantRotation.R0),
                f(Direction.NORTH, "", BlockStateVariantRotation.R180),
                f(Direction.EAST, "", BlockStateVariantRotation.R270),
                f(Direction.SOUTH, "", BlockStateVariantRotation.R0),
                f(Direction.WEST, "", BlockStateVariantRotation.R90),
            )
        }
        card.block.registerCutoutRenderLayer()

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
    val block = configuration.createBlock()
    val item = BlockItem(block, Item.Settings())
    val poemList = PoemList(configuration.tier).poem(configuration.poem)
}

open class FairyLogisticsBlock(settings: Settings) : SimpleHorizontalFacingBlock(settings) { // TODO
    companion object {
        private val SHAPES: Array<VoxelShape> = arrayOf(
            createCuboidShape(2.0, 2.0, 0.0, 14.0, 14.0, 8.0), // DOWN
            createCuboidShape(2.0, 2.0, 0.0, 14.0, 14.0, 8.0), // UP
            createCuboidShape(2.0, 2.0, 0.0, 14.0, 14.0, 8.0), // NORTH
            createCuboidShape(2.0, 2.0, 8.0, 14.0, 14.0, 16.0), // SOUTH
            createCuboidShape(0.0, 2.0, 2.0, 8.0, 14.0, 14.0), // WEST
            createCuboidShape(8.0, 2.0, 2.0, 16.0, 14.0, 14.0), // EAST
        )
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState = if (ctx.side.axis == Direction.Axis.Y) super.getPlacementState(ctx) else defaultState.with(FACING, ctx.side.opposite)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = SHAPES[state.get(FACING).id]

}
