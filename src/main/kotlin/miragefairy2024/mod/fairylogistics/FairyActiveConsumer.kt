package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

object FairyActiveConsumerCard : FairyLogisticsCard<FairyActiveConsumerBlock, FairyActiveConsumerBlockEntity, FairyActiveConsumerScreenHandler>() {
    override fun getPath() = "fairy_active_consumer"
    override val tier = 3
    override val name = EnJa("Fairy Active Consumer(WIP)", "妖精の郵便受け(WIP)") // TODO rename
    override val poem = EnJa("Tonight, I'll Be Eating...", "焼き鯖だよ――")
    override val description = EnJa("The ordered items are delivered", "注文したアイテムが搬入される")

    override fun createBlockSettings(): FabricBlockSettings = super.createBlockSettings().mapColor(MapColor.PALE_PURPLE).sounds(BlockSoundGroup.METAL)
    override fun createBlock() = FairyActiveConsumerBlock(this)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyActiveConsumerBlockEntity)

    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FairyActiveConsumerScreenHandler(this, arguments)

    override val guiWidth = 176
    override val guiHeight = 168

    class Slot(
        override val x: Int,
        override val y: Int,
    ) : MachineBlockEntity.InventorySlotConfiguration, MachineScreenHandler.GuiSlotConfiguration {
        override fun isValid(itemStack: ItemStack) = true
        override fun canInsert(direction: Direction) = true
        override fun canExtract(direction: Direction) = true
        override val isObservable = false
        override val dropItem = true
        override fun getTooltip() = null
    }

    val SLOTS = (0 until 3).flatMap { c ->
        (0 until 9).map { r ->
            Slot(8 + 18 * r, 19 + 18 * c)
        }
    }
    val FILTER_SLOT = SLOTS.last()

    context(ModContext)
    override fun init() {
        super.init()

        inventorySlotConfigurations += SLOTS
        guiSlotConfigurations += SLOTS


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

class FairyActiveConsumerBlock(card: FairyActiveConsumerCard) : FairyLogisticsBlock(card) {
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
}

class FairyActiveConsumerBlockEntity(card: FairyActiveConsumerCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyActiveConsumerBlockEntity>(card, pos, state) {
    override fun getThis() = this

    override fun serverTick(world: World, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        // 1分に1回発動する

        // 対象範囲のBlockEntityを列挙する

        // 視線判定

        // すべてのBlockEntityに対して、フィルタにマッチするアイテムを片っ端から輸送させる
        // ただし残りの輸送速度を超えない

        // 配送所の輸送済みカウントを増やす
        // 配送所の輸送済みカウントは配送所側が毎分初期化する

        // 取れるだけ空きスロットに詰めていく
        // 空きスロットが無ければ終了

        // 最初と最後に接続先ブロックへの輸送を試みる

    }
}

class FairyActiveConsumerScreenHandler(card: FairyActiveConsumerCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
