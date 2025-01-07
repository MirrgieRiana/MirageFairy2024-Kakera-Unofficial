package miragefairy2024.mod.fairybuilding

import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.collectItem
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.invoke
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.set
import miragefairy2024.util.text
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object FairyCollectorConfiguration : FairyFactoryConfiguration<FairyCollectorCard, FairyCollectorConfiguration, FairyCollectorBlock, FairyCollectorBlockEntity, FairyCollectorScreenHandler>() {
    override val path = "fairy_collector"
    override val tier = 2
    override val name = EnJa("Fairy Collector", "いたずら妖精エンデルマーニャの隠れ家")
    override val poem = EnJa("An attractor of curiosity", "あれ？ここにあったリモコン知らない？")

    override fun createBlock(cardGetter: () -> FairyCollectorCard, settings: FabricBlockSettings) = FairyCollectorBlock(cardGetter, settings)

    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FairyCollectorBlockEntity)

    override fun createScreenHandler(card: FairyCollectorCard, arguments: FairyBuildingScreenHandler.Arguments) = FairyCollectorScreenHandler(card, arguments)

    override val guiWidth = 176
    override val guiHeight = 162

    override fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> {
        val extractDirections = setOf(Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.WEST, Direction.EAST)
        return super.createSlotConfigurations() + listOf(
            FairyBuildingSlotConfiguration(15, 35, toolTipGetter = { listOf(text { SPECIFIED_FAIRY_SLOT_TRANSLATION(MotifCard.CARRY.displayName) }) }) { isFairy(it, MotifCard.CARRY) }, // 回収妖精 // TODO 妖精パーティクル
            FairyBuildingSlotConfiguration(37 + 18 * 0, 17 + 18 * 0, animation = SlotAnimationConfiguration(false, listOf(Position(11.5, 1.5, 2.5, 0.0F, 180.0F, 200)))), // 机
            FairyBuildingSlotConfiguration(81, 35, animation = SlotAnimationConfiguration(true, run {
                listOf(
                    Position(11.5, 0.1, 6.0, 0.0F, 90.0F, 40),
                    Position(7.0, 0.1, 8.0, 0.0F, 275.0F, 40),
                    Position(12.0, 0.1, 8.0, 0.0F, 265.0F, 40),
                    Position(8.0, 0.1, 6.0, 0.0F, 20.0F, 40),
                )
            })) { it.isOf(FairyCard.item) }, // 仕分け妖精
            FairyBuildingSlotConfiguration(106 + 18 * 0, 26 + 18 * 0, extractDirections = extractDirections, animation = SlotAnimationConfiguration(false, listOf(Position(4.0, 2.0, 4.5, 0.0F, 270.0F, 200)))), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 1, 26 + 18 * 0, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 2, 26 + 18 * 0, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 0, 26 + 18 * 1, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 1, 26 + 18 * 1, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(106 + 18 * 2, 26 + 18 * 1, extractDirections = extractDirections), // 箱
            FairyBuildingSlotConfiguration(37 + 18 * 1, 17 + 18 * 0), // 机
            FairyBuildingSlotConfiguration(37 + 18 * 0, 17 + 18 * 1), // 机
            FairyBuildingSlotConfiguration(37 + 18 * 1, 17 + 18 * 1), // 机
        )
    }

    val TABLE_SLOT_INDICES = listOf(1, 9, 10, 11)
    val CHEST_SLOT_INDICES = 3..8

    val COLLECTION_PROGRESS_PROPERTY = FairyBuildingPropertyConfiguration<FairyCollectorBlockEntity>({ collectionProgress }, { collectionProgress = it })
    val SORT_PROGRESS_PROPERTY = FairyBuildingPropertyConfiguration<FairyCollectorBlockEntity>({ sortProgress }, { sortProgress = it })
    val COLLECTION_SPEED_PROPERTY = FairyBuildingPropertyConfiguration<FairyCollectorBlockEntity>({ collectionSpeed }, { collectionSpeed = it })
    val SORT_SPEED_PROPERTY = FairyBuildingPropertyConfiguration<FairyCollectorBlockEntity>({ sortSpeed }, { sortSpeed = it })
    override fun createPropertyConfigurations() = super.createPropertyConfigurations() + listOf(
        COLLECTION_PROGRESS_PROPERTY,
        SORT_PROGRESS_PROPERTY,
        COLLECTION_SPEED_PROPERTY,
        SORT_SPEED_PROPERTY,
    )

    override val collectingFolia = 10_000
    override val maxFolia = 20_000

    context(ModContext)
    override fun init(card: FairyCollectorCard) {
        super.init(card)
        registerShapedRecipeGeneration(card.item) {
            pattern(" C ")
            pattern("C#C")
            pattern(" C ")
            input('#', FairyHouseCard.item)
            input('C', Items.CHEST)
        } on FairyHouseCard.item
    }
}

object FairyCollectorCard : FairyFactoryCard<FairyCollectorCard, FairyCollectorConfiguration, FairyCollectorBlock, FairyCollectorBlockEntity, FairyCollectorScreenHandler>(FairyCollectorConfiguration) {
    override val self = this
}

class FairyCollectorBlock(cardGetter: () -> FairyCollectorCard, settings: Settings) : FairyFactoryBlock<FairyCollectorCard>(cardGetter, settings)

class FairyCollectorBlockEntity(card: FairyCollectorCard, pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyCollectorCard, FairyCollectorBlockEntity>(card, pos, state) {

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


    override fun serverTick(world: World, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        if (folia < 3_000) {
            setStatus(FairyFactoryBlock.Status.OFFLINE)
            return
        }

        folia -= 10

        collectionProgress += collectionSpeed
        if (collectionProgress >= 10000) {
            collectionProgress = 0

            val indices = FairyCollectorConfiguration.TABLE_SLOT_INDICES.filter { this[it].isEmpty }.toCollection(ArrayDeque())
            if (indices.isNotEmpty()) {

                folia -= 1000

                val region = BlockBox(pos.x - 10, pos.y - 4, pos.z - 10, pos.x + 10, pos.y, pos.z + 10)
                collectItem(world, pos, region = region, ignoreOriginalWall = true) {

                    folia -= 500 + 30 * it.stack.count

                    val index = indices.removeFirst()
                    this[index] = it.stack.copy()
                    it.discard()
                    // TODO パーティクル

                    indices.isNotEmpty()
                }

            }
        }

        sortProgress += sortSpeed
        if (sortProgress >= 10000) {
            sortProgress = 0

            folia -= 200

            val result = mergeInventory(this, FairyCollectorConfiguration.TABLE_SLOT_INDICES, this, FairyCollectorConfiguration.CHEST_SLOT_INDICES)
            folia -= 20 * result.movedItemCount

        }

        setStatus(FairyFactoryBlock.Status.PROCESSING)
    }

}

class FairyCollectorScreenHandler(card: FairyCollectorCard, arguments: Arguments) : FairyFactoryScreenHandler<FairyCollectorCard>(card, arguments) {
    var collectionProgress by Property(FairyCollectorConfiguration.COLLECTION_PROGRESS_PROPERTY)
    var sortProgress by Property(FairyCollectorConfiguration.SORT_PROGRESS_PROPERTY)
    var collectionSpeed by Property(FairyCollectorConfiguration.COLLECTION_SPEED_PROPERTY)
    var sortSpeed by Property(FairyCollectorConfiguration.SORT_SPEED_PROPERTY)
}
