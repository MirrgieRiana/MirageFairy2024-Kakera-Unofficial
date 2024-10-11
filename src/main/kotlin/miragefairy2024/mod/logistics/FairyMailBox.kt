package miragefairy2024.mod.logistics

import miragefairy2024.ModContext
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

object FairyMailboxConfiguration : FairyLogisticsBlockConfiguration() {
    override val path = "fairy_mailbox"
    override val name = EnJa("Fairy Mailbox", "妖精の郵便受け")
    override val tier = 3
    override val poem = EnJa("TODO", "TODO") // TODO
    override fun createBlock() = FairyMailboxBlock(createFairyLogisticsBlockSettings().mapColor(MapColor.PALE_PURPLE).sounds(BlockSoundGroup.METAL))

    context(ModContext)
    override fun init(card: FairyLogisticsBlockCard) {
        super.init(card)

        registerShapedRecipeGeneration(card.item) {
            pattern("#A#")
            pattern("DCD")
            pattern("###")
            input('A', BlockMaterialCard.AURA_STONE.item)
            input('#', Items.IRON_INGOT)
            input('C', Items.ITEM_FRAME)
            input('D', Items.LIGHT_BLUE_DYE)
        } on BlockMaterialCard.AURA_STONE.item
    }
}

object FairyMailboxCard : FairyLogisticsBlockCard(FairyMailboxConfiguration)

class FairyMailboxBlock(settings: Settings) : FairyLogisticsBlock(settings) {
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

    //override fun createBlockEntity(pos: BlockPos, state: BlockState) = FairyStatueBlockEntity(card, pos, state)

    /*
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.onPlaced(world, pos, state, placer, itemStack)
        if (world.isClient) return
        val blockEntity = world.getBlockEntity(pos) as? FairyStatueBlockEntity ?: return
        blockEntity.setMotif(itemStack.getFairyStatueMotif())
    }
    */

}
