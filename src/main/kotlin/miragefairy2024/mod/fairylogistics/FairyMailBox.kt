package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
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

object FairyMailboxCard : FairyLogisticsCard<FairyMailboxBlock, FairyMailboxBlockEntity, FairyMailboxScreenHandler>() {
    override fun getPath() = "fairy_mailbox"
    override val tier = 3
    override val name = EnJa("Fairy Mailbox", "妖精の郵便受け")
    override val poem = EnJa("TODO", "TODO") // TODO

    override fun createBlockSettings() = super.createBlockSettings().mapColor(MapColor.PALE_PURPLE).sounds(BlockSoundGroup.METAL)
    override fun createBlock() = FairyMailboxBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyMailboxBlockEntity)

    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyMailboxScreenHandler(this, arguments)

    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(item) {
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

class FairyMailboxBlock(card: FairyMailboxCard) : FairyLogisticsBlock(card) {
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

class FairyMailboxBlockEntity(card: FairyMailboxCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyMailboxBlockEntity>(card, pos, state) {
    override fun getThis() = this

    // TODO

}

class FairyMailboxScreenHandler(card: FairyMailboxCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
