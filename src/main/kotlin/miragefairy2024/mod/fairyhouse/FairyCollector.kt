package miragefairy2024.mod.fairyhouse

import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.getFairyCondensation
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.util.collectItem
import miragefairy2024.util.get
import miragefairy2024.util.insertItem
import miragefairy2024.util.int
import miragefairy2024.util.inventoryAccessor
import miragefairy2024.util.itemStacks
import miragefairy2024.util.on
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.wrapper
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.log

object FairyCollectorCard : FairyFactoryCard<FairyCollectorBlockEntity, FairyFactoryScreenHandler>(
    "fairy_collector", 2, "Fairy Collector", "エンデルマーニャの隠れ家",
    "TODO", "あれ？ここにあったリモコン知らない？", // TODO
    { FairyFactoryBlock({ FairyCollectorCard }, it) },
    BlockEntityAccessor(::FairyCollectorBlockEntity),
    { FairyFactoryScreenHandler(FairyCollectorCard, it) },
    176, 162,
    AbstractFairyHouseBlockEntity.Settings(
        slots = listOf(
            AbstractFairyHouseBlockEntity.SlotSettings(19, 34) { FairyCollectorCard.isValid(it) }, // 回収妖精
            AbstractFairyHouseBlockEntity.SlotSettings(39, 34, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 机
            AbstractFairyHouseBlockEntity.SlotSettings(76, 34, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)) { it.isOf(FairyCard.item) }, // 仕分け妖精
            AbstractFairyHouseBlockEntity.SlotSettings(101 + 18 * 0, 25 + 18 * 0, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(101 + 18 * 1, 25 + 18 * 0, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(101 + 18 * 2, 25 + 18 * 0, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(101 + 18 * 0, 25 + 18 * 1, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(101 + 18 * 1, 25 + 18 * 1, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 籠
            AbstractFairyHouseBlockEntity.SlotSettings(101 + 18 * 2, 25 + 18 * 1, appearance = AbstractFairyHouseBlockEntity.Appearance(4.5, 2.2, 14.0, 90.0, 270.0)), // 籠
        ),
    ),
) {
    private fun isValid(itemStack: ItemStack): Boolean {
        if (!itemStack.isOf(FairyCard.item)) return false
        val motif = itemStack.getFairyMotif() ?: return false
        return when (motif) {
            MotifCard.HOPPER, MotifCard.ENDERMAN -> true // TODO 系統
            else -> false
        }
    }

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

private fun getFairyLevel(itemStack: ItemStack): Double {
    if (!itemStack.isOf(FairyCard.item)) return 0.0
    val motif = itemStack.getFairyMotif() ?: return 0.0
    val count = itemStack.getFairyCondensation() * itemStack.count
    val level = motif.rare.toDouble() + log(count.toDouble(), 3.0)
    return level
}

class FairyCollectorBlockEntity(pos: BlockPos, state: BlockState) : FairyFactoryBlockEntity<FairyCollectorBlockEntity>(FairyCollectorCard, pos, state) {
    override val self = this

    private var collectionProgress = 0
    private var sortProgress = 0

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


    private var collectionSpeed = 0
    private var sortSpeed = 0

    override fun markDirty() {
        super.markDirty()
        updateCache()
    }

    private fun updateCache() {
        collectionSpeed = (10000.0 / (20.0 * 3.0) / 10.0 * getFairyLevel(this[0])).toInt()
        sortSpeed = (10000.0 / (20.0 * 2.0) / 10.0 * getFairyLevel(this[2])).toInt()
    }


    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)
        var folia = getFolia()

        collectionProgress += collectionSpeed
        if (collectionProgress >= 10000) {
            collectionProgress = 0



        }

        sortProgress += sortSpeed
        if (sortProgress >= 10000) {
            sortProgress = 0



        }

        setFolia(folia)

        

        if (collectionCooldown > 0) {
            collectionCooldown--
        } else if (collectionCooldown <= -1) {
            collectionCooldown = world.random.nextInt(20 * 10)
        } else {
            collectionCooldown = 20 * 10

            val status = run {
                if (folia < 10) return@run FairyFactoryBlock.Status.OFFLINE // フォリアが足りない

                if (!itemStacks.any { it.isEmpty }) return@run FairyFactoryBlock.Status.IDLE // 負荷軽減のために1スロも空いていない場合は止める

                var collected = false
                val region = BlockBox(pos.x - 10, pos.y - 4, pos.z - 10, pos.x + 10, pos.y, pos.z + 10)
                collectItem(world, pos, maxCount = 2, reach = 30, region = region, ignoreOriginalWall = true) {
                    if (folia < 10) return@collectItem false
                    val itemStack = it.stack.copy()
                    if (inventoryAccessor.insertItem(itemStack, 3..9)) {
                        folia -= 10
                        collected = true
                    }
                    if (itemStack.isEmpty) {
                        it.discard()
                    } else {
                        it.stack = itemStack
                    }
                    true
                }

                if (collected) {
                    FairyFactoryBlock.Status.PROCESSING
                } else {
                    FairyFactoryBlock.Status.IDLE
                }
            }
            setStatus(status)

        }
    }
}
