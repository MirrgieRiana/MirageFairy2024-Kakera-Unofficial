package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.item.Items
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

object FairyDistributionCenterCard : FairyLogisticsCard<FairyDistributionCenterBlock, FairyDistributionCenterBlockEntity, FairyDistributionCenterScreenHandler>() {
    override fun getPath() = "fairy_distribution_center"
    override val tier = 3
    override val name = EnJa("Fairy Distribution Center", "妖精のお届けもの屋")
    override val poem = EnJa("TODO", "TODO") // TODO

    override fun createBlockSettings() = super.createBlockSettings().mapColor(MapColor.PINK).sounds(BlockSoundGroup.WOOD)
    override fun createBlock() = FairyDistributionCenterBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyDistributionCenterBlockEntity)

    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyDistributionCenterScreenHandler(this, arguments)

    context(ModContext)
    override fun init() {
        super.init()

        block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }

        registerShapedRecipeGeneration(item) {
            pattern("#A#")
            pattern("DCD")
            pattern("###")
            input('A', BlockMaterialCard.AURA_STONE.item)
            input('#', ItemTags.PLANKS)
            input('C', Items.BARREL)
            input('D', Items.PINK_DYE)
        } on BlockMaterialCard.AURA_STONE.item
    }
}

class FairyDistributionCenterBlock(card: FairyDistributionCenterCard) : FairyLogisticsBlock(card) {
    companion object {
        private val SHAPES: Array<VoxelShape> = arrayOf(
            // UP
            createCuboidShape(2.0, 4.0, 8.0, 14.0, 16.0, 16.0), // SOUTH
            createCuboidShape(0.0, 4.0, 2.0, 8.0, 16.0, 14.0), // WEST
            createCuboidShape(2.0, 4.0, 0.0, 14.0, 16.0, 8.0), // NORTH
            createCuboidShape(8.0, 4.0, 2.0, 16.0, 16.0, 14.0), // EAST

            // SIDE
            createCuboidShape(2.0, 2.0, 8.0, 14.0, 14.0, 16.0), // SOUTH
            createCuboidShape(0.0, 2.0, 2.0, 8.0, 14.0, 14.0), // WEST
            createCuboidShape(2.0, 2.0, 0.0, 14.0, 14.0, 8.0), // NORTH
            createCuboidShape(8.0, 2.0, 2.0, 16.0, 14.0, 14.0), // EAST

            // DOWN
            createCuboidShape(2.0, 0.0, 8.0, 14.0, 12.0, 16.0), // SOUTH
            createCuboidShape(0.0, 0.0, 2.0, 8.0, 12.0, 14.0), // WEST
            createCuboidShape(2.0, 0.0, 0.0, 14.0, 12.0, 8.0), // NORTH
            createCuboidShape(8.0, 0.0, 2.0, 16.0, 12.0, 14.0), // EAST
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = SHAPES[4 * state[VERTICAL_FACING].id + state[FACING].horizontal]

    // TODO

}

class FairyDistributionCenterBlockEntity(card: FairyDistributionCenterCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyDistributionCenterBlockEntity>(card, pos, state) {
    override fun getThis() = this

    // TODO

}

class FairyDistributionCenterScreenHandler(card: FairyDistributionCenterCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
