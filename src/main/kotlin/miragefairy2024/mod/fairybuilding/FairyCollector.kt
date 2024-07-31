package miragefairy2024.mod.fairybuilding

import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.util.collectItem
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.set
import miragefairy2024.util.wrapper
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object FairyCollectorCard : FairyFactoryCard<FairyCollectorBlockEntity, FairyCollectorScreenHandler>(
    "fairy_collector", 2, "Fairy Collector", "いたずら妖精エンデルマーニャの隠れ家",
    "An attractor of curiosity", "あれ？ここにあったリモコン知らない？",
    { FairyFactoryBlock({ FairyCollectorCard }, it) },
    BlockEntityAccessor(::FairyCollectorBlockEntity),
    { FairyCollectorScreenHandler(it) },
    176, 162,
    FairyBuildingBlockEntity.Settings(
        slots = run {
            val extractDirections = setOf(Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.WEST, Direction.EAST)
            listOf(
                FairyBuildingBlockEntity.SlotSettings(19, 34) { isFairy(it, MotifCard.CARRY) }, // 回収妖精 // TODO 妖精パーティクル
                FairyBuildingBlockEntity.SlotSettings(39, 34, appearance = FairyBuildingBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 机
                FairyBuildingBlockEntity.SlotSettings(76, 34, appearance = FairyBuildingBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // 仕分け妖精
                FairyBuildingBlockEntity.SlotSettings(101 + 18 * 0, 25 + 18 * 0, extractDirections = extractDirections, appearance = FairyBuildingBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 籠
                FairyBuildingBlockEntity.SlotSettings(101 + 18 * 1, 25 + 18 * 0, extractDirections = extractDirections), // 籠
                FairyBuildingBlockEntity.SlotSettings(101 + 18 * 2, 25 + 18 * 0, extractDirections = extractDirections), // 籠
                FairyBuildingBlockEntity.SlotSettings(101 + 18 * 0, 25 + 18 * 1, extractDirections = extractDirections), // 籠
                FairyBuildingBlockEntity.SlotSettings(101 + 18 * 1, 25 + 18 * 1, extractDirections = extractDirections), // 籠
                FairyBuildingBlockEntity.SlotSettings(101 + 18 * 2, 25 + 18 * 1, extractDirections = extractDirections), // 籠
            )
        },
        properties = listOf(
            FairyCollectorScreenHandler.COLLECTION_PROGRESS_PROPERTY,
            FairyCollectorScreenHandler.SORT_PROGRESS_PROPERTY,
            FairyCollectorScreenHandler.COLLECTION_SPEED_PROPERTY,
            FairyCollectorScreenHandler.SORT_SPEED_PROPERTY,
        ),
    ),
) {
    context(ModContext)
    override fun init() {
        super.init()
        registerShapedRecipeGeneration(FairyCollectorCard.item) {
            pattern("BCB")
            pattern("C#C")
            pattern("BCB")
            input('#', FairyHouseCard.item)
            input('C', Items.CHEST)
            input('B', Items.BOWL)
        } on FairyHouseCard.item
    }
}

class FairyCollectorBlockEntity(pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyCollectorBlockEntity>(FairyCollectorCard, pos, state) {

    override val self = this


    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        collectionProgress = nbt.wrapper["CollectionProgress"].int.get() ?: 0
        sortProgress = nbt.wrapper["SortProgress"].int.get() ?: 0
        updateCache()
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["CollectionProgress"].int.set(collectionProgress)
        nbt.wrapper["SortProgress"].int.set(sortProgress)
    }


    var collectionProgress = 0
    var sortProgress = 0
    var collectionSpeed = 0
    var sortSpeed = 0


    override fun markDirty() {
        super.markDirty()
        updateCache()
    }

    private fun updateCache() {
        collectionSpeed = (10000.0 / (20.0 * 3.0) * (getFairyLevel(this[0]) / 10.0)).toInt()
        sortSpeed = (10000.0 / (20.0 * 2.0) * (getFairyLevel(this[2]) / 10.0)).toInt()
    }


    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)

        if (folia < 10_000) {
            setStatus(FairyFactoryBlock.Status.OFFLINE)
            return
        }

        folia -= 10

        if (this[1].isEmpty) {
            collectionProgress += collectionSpeed
            if (collectionProgress >= 10000) run {
                collectionProgress = 0

                folia -= 1000

                val region = BlockBox(pos.x - 10, pos.y - 4, pos.z - 10, pos.x + 10, pos.y, pos.z + 10)
                collectItem(world, pos, maxCount = 1, reach = 30, region = region, ignoreOriginalWall = true) {

                    folia -= 500 + 30 * it.stack.count

                    this[1] = it.stack.copy()
                    it.discard()

                    true
                }

            }
        }

        if (this[1].isNotEmpty) {
            sortProgress += sortSpeed
            if (sortProgress >= 10000) {
                sortProgress = 0

                folia -= 200

                val result = mergeInventory(this, 1, this, 3..8)
                folia -= 20 * result.movedItemCount

            }
        }

        setStatus(FairyFactoryBlock.Status.PROCESSING)
    }

}

class FairyCollectorScreenHandler(arguments: Arguments) : FairyFactoryScreenHandler(FairyCollectorCard, arguments) {
    companion object {
        val COLLECTION_PROGRESS_PROPERTY = FairyBuildingBlockEntity.PropertySettings<FairyCollectorBlockEntity>({ collectionProgress }, { collectionProgress = it })
        val SORT_PROGRESS_PROPERTY = FairyBuildingBlockEntity.PropertySettings<FairyCollectorBlockEntity>({ sortProgress }, { sortProgress = it })
        val COLLECTION_SPEED_PROPERTY = FairyBuildingBlockEntity.PropertySettings<FairyCollectorBlockEntity>({ collectionSpeed }, { collectionSpeed = it })
        val SORT_SPEED_PROPERTY = FairyBuildingBlockEntity.PropertySettings<FairyCollectorBlockEntity>({ sortSpeed }, { sortSpeed = it })
    }

    var collectionProgress: Int
        get() = arguments.propertyDelegate.get(card.propertyIndexTable[COLLECTION_PROGRESS_PROPERTY]!!)
        set(value) {
            arguments.propertyDelegate.set(card.propertyIndexTable[COLLECTION_PROGRESS_PROPERTY]!!, value)
        }

    var sortProgress: Int
        get() = arguments.propertyDelegate.get(card.propertyIndexTable[SORT_PROGRESS_PROPERTY]!!)
        set(value) {
            arguments.propertyDelegate.set(card.propertyIndexTable[SORT_PROGRESS_PROPERTY]!!, value)
        }

    var collectionSpeed: Int
        get() = arguments.propertyDelegate.get(card.propertyIndexTable[COLLECTION_SPEED_PROPERTY]!!)
        set(value) {
            arguments.propertyDelegate.set(card.propertyIndexTable[COLLECTION_SPEED_PROPERTY]!!, value)
        }

    var sortSpeed: Int
        get() = arguments.propertyDelegate.get(card.propertyIndexTable[SORT_SPEED_PROPERTY]!!)
        set(value) {
            arguments.propertyDelegate.set(card.propertyIndexTable[SORT_SPEED_PROPERTY]!!, value)
        }

}
