package miragefairy2024.mod.fairylogistics

import miragefairy2024.ModContext
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.EnJa
import miragefairy2024.util.hasSameItemAndNbt
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.toInventoryDelegate
import miragefairy2024.util.toSidedInventoryDelegate
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.RaycastContext
import net.minecraft.world.World

// TODO WIP
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
    val CONTAINER_SLOTS = SLOTS.filter { it != FILTER_SLOT }

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

class FairyActiveConsumerBlockEntity(private val card: FairyActiveConsumerCard, pos: BlockPos, state: BlockState) : FairyLogisticsBlockEntity<FairyActiveConsumerBlockEntity>(card, pos, state) {
    companion object {
        private fun getSquaredDistance(a: BlockPos, b: BlockPos): Int {
            val dx = a.x - b.x
            val dy = a.y - b.y
            val dz = a.z - b.z
            return dx * dx + dy * dy + dz * dz
        }
    }

    override fun getThis() = this

    private var t = -1

    override fun serverTick(world: World, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        // 1分に1回発動する
        if (t == -1) t = world.random.nextInt(20 * 60)
        t--
        if (t > 0) return
        t = 20 * 60

        // 一旦すべてのアイテムを配送
        run {
            val (targetInventory, targetSide) = getTarget() ?: return@run
            val result = mergeInventory(
                this.toInventoryDelegate(),
                targetInventory.toSidedInventoryDelegate(targetSide),
                srcIndices = FairyActiveConsumerCard.CONTAINER_SLOTS.mapNotNull { card.inventorySlotIndexTable[it] },
            )
            if (result.movedItemCount > 0) markDirty()
        }

        // フィルタを取得
        val filterItemStack = getStack(card.inventorySlotIndexTable[FairyActiveConsumerCard.FILTER_SLOT]!!)
        if (filterItemStack.isEmpty) return

        // 空きスロットを列挙
        val availableDestIndices = FairyActiveConsumerCard.CONTAINER_SLOTS
            .mapNotNull { card.inventorySlotIndexTable[it] }
            .filter { getStack(it).isEmpty }
            .toMutableList()
        if (availableDestIndices.isEmpty()) return

        // 対象範囲の配送所を列挙する
        val centerChunkX = ChunkSectionPos.getSectionCoord(pos.x)
        val centerChunkZ = ChunkSectionPos.getSectionCoord(pos.z)
        val neighbourChunksSuppliers = (centerChunkX - 1..centerChunkX + 1).flatMap { chunkX ->
            (centerChunkZ - 1..centerChunkZ + 1).flatMap { chunkZ ->
                world.getChunk(chunkX, chunkZ).blockEntities.values.mapNotNull { it as? FairyPassiveSupplierBlockEntity } // TODO interface
            }
        }
        val reachingSuppliers = neighbourChunksSuppliers.filter { getSquaredDistance(pos, it.pos) <= 16 * 16 }

        // 視線判定
        val posD = pos.toCenterPos()
        val entity = ArrowEntity(world, posD.x, posD.y, posD.z)
        val unblockedSuppliers = reachingSuppliers.filter { supplier ->
            val supplierPosD = supplier.pos.toCenterPos()
            val hitResult = world.raycast(RaycastContext(posD, supplierPosD, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity))
            hitResult.type == HitResult.Type.MISS
        }

        // すべてのSupplierに対して、フィルタにマッチするアイテムを片っ端から輸送させる
        // ただし残りの輸送速度を超えない
        var changed = false
        run finishSuppliers@{
            unblockedSuppliers.forEach { supplier ->
                if (supplier.logisticsEnergy == 0) return@forEach
                val (supplierTargetInventory, supplierTargetSide) = supplier.getTarget() ?: return@forEach
                val src = supplierTargetInventory.toSidedInventoryDelegate(supplierTargetSide)

                var srcChanged = false
                run finishSrcIndices@{
                    src.getIndices().forEach nextSrcIndex@{ srcIndex ->
                        val srcItemStack = src.getItemStack(srcIndex)
                        if (srcItemStack.isEmpty) return@nextSrcIndex
                        if (!(srcItemStack hasSameItemAndNbt filterItemStack)) return@nextSrcIndex
                        if (!src.canExtract(srcIndex, srcItemStack)) return@nextSrcIndex
                        val destIndex = availableDestIndices.removeFirst()
                        src.setItemStack(srcIndex, EMPTY_ITEM_STACK)
                        srcChanged = true
                        setStack(destIndex, srcItemStack)
                        changed = true
                        if (availableDestIndices.isEmpty()) return@finishSrcIndices
                    }
                }
                if (srcChanged) src.markDirty()

                if (availableDestIndices.isEmpty()) return@finishSuppliers
            }
        }
        if (changed) markDirty()

    }
}

class FairyActiveConsumerScreenHandler(card: FairyActiveConsumerCard, arguments: Arguments) : FairyLogisticsScreenHandler(card, arguments)
