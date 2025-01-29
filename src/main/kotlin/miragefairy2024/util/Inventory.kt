package miragefairy2024.util

import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import mirrg.kotlin.hydrogen.unit
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.util.math.Direction
import kotlin.experimental.and

operator fun Inventory.get(slot: Int): ItemStack = this.getStack(slot)
operator fun Inventory.set(slot: Int, stack: ItemStack) = this.setStack(slot, stack)
val Inventory.size get() = this.size()
val Inventory.indices get() = 0 until this.size
val Inventory.itemStacks get() = this.indices.map { this[it] }

class FilteringSlot(inventory: Inventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun canInsert(stack: ItemStack) = inventory.isValid(index, stack)
}

class OutputSlot(inventory: Inventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun canInsert(stack: ItemStack) = false
}


// Merge

/**
 * @param completed 移動が完了したかどうか
 * @param movementTimes アイテムの移動を行った回数
 * @param movedItemCount 移動されたアイテムの個数
 */
class MergeResult(val completed: Boolean, val movementTimes: Int, val movedItemCount: Int) {
    companion object {
        val UP_TO_DATE = MergeResult(true, 0, 0)
        val FAILED = MergeResult(false, 0, 0)
    }
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(src: InventoryDelegate, dest: InventoryDelegate, srcSlotIndex: Int, destSlotIndex: Int): MergeResult {

    val srcItemStack = src.getItemStack(srcSlotIndex)
    if (srcItemStack.isEmpty) return MergeResult.UP_TO_DATE // 元から空なので何もする必要はない

    if (!dest.canInsert(destSlotIndex, srcItemStack)) return MergeResult.FAILED // 宛先にこの種類のアイテムが入らない

    val destItemStack = dest.getItemStack(destSlotIndex)
    if (destItemStack.isNotEmpty && !(srcItemStack hasSameItemAndNbt destItemStack)) return MergeResult.FAILED // 宛先に別のアイテムが入っているので何もできない

    // 先が空もしくは元と同じ種類のアイテムが入っているのでマージ

    // 個数計算
    val srcCount = srcItemStack.count
    val oldDestCount = destItemStack.count
    val allCount = srcCount + oldDestCount
    val newDestCount = allCount atMost dest.getMaxCountPerStack(destSlotIndex) atMost srcItemStack.maxCount atLeast oldDestCount
    val moveCount = newDestCount - oldDestCount

    if (moveCount == 0) return MergeResult.FAILED // 宛先にこれ以上入らない

    // 移動処理
    if (destItemStack.isEmpty) {
        dest.setItemStack(destSlotIndex, srcItemStack.copyWithCount(newDestCount))
    } else {
        destItemStack.count = newDestCount
    }
    val newSrcCount = allCount - newDestCount
    srcItemStack.count = newSrcCount
    if (newSrcCount == 0) src.setItemStack(srcSlotIndex, EMPTY_ITEM_STACK)

    return MergeResult(newSrcCount == 0, 1, moveCount)
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(src: InventoryDelegate, dest: InventoryDelegate, srcIndex: Int, destIndices: Iterable<Int>): MergeResult {
    if (!src.canExtract(srcIndex, src.getItemStack(srcIndex))) return MergeResult.FAILED // 移動元がこのアイテムを出せない
    var movementTimes = 0
    var movedItemCount = 0
    destIndices.forEach { destIndex ->
        val result = mergeInventory(src, dest, srcIndex, destIndex)
        movementTimes += result.movementTimes
        movedItemCount += result.movedItemCount
        if (result.completed) return MergeResult(true, movementTimes, movedItemCount) // すべてマージされたのでこれ以降の判定は不要
    }
    return MergeResult(false, movementTimes, movedItemCount)
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(src: InventoryDelegate, dest: InventoryDelegate, srcIndices: Iterable<Int>? = null, destIndices: Iterable<Int>? = null): MergeResult {
    var completed = true
    var movementTimes = 0
    var movedItemCount = 0
    (srcIndices ?: src.getIndices()).forEach { srcIndex ->
        val result = mergeInventory(src, dest, srcIndex, destIndices ?: dest.getIndices())
        if (!result.completed) completed = false
        movementTimes += result.movementTimes
        movedItemCount += result.movedItemCount
    }
    return MergeResult(completed, movementTimes, movedItemCount)
}

interface InventoryDelegate {
    fun getIndices(): Iterable<Int>
    fun getItemStack(index: Int): ItemStack
    fun setItemStack(index: Int, itemStack: ItemStack)
    fun canInsert(index: Int, itemStack: ItemStack): Boolean
    fun canExtract(index: Int, itemStack: ItemStack): Boolean
    fun getMaxCountPerStack(index: Int): Int
    fun markDirty()
}

class MutableListInventoryDelegate(private val inventory: MutableList<ItemStack>) : InventoryDelegate {
    override fun getIndices() = inventory.indices
    override fun getItemStack(index: Int) = inventory[index]
    override fun setItemStack(index: Int, itemStack: ItemStack) = unit { inventory[index] = itemStack }
    override fun canExtract(index: Int, itemStack: ItemStack) = true
    override fun canInsert(index: Int, itemStack: ItemStack) = true
    override fun getMaxCountPerStack(index: Int) = 64
    override fun markDirty() = Unit
}

fun MutableList<ItemStack>.toInventoryDelegate() = MutableListInventoryDelegate(this)

class SimpleInventoryDelegate(private val inventory: Inventory) : InventoryDelegate {
    override fun getIndices() = inventory.indices
    override fun getItemStack(index: Int) = inventory[index]
    override fun setItemStack(index: Int, itemStack: ItemStack) = unit { inventory[index] = itemStack }
    override fun canExtract(index: Int, itemStack: ItemStack) = true
    override fun canInsert(index: Int, itemStack: ItemStack) = inventory.isValid(index, itemStack)
    override fun getMaxCountPerStack(index: Int) = inventory.maxCountPerStack
    override fun markDirty() = inventory.markDirty()
}

fun Inventory.toInventoryDelegate() = SimpleInventoryDelegate(this)

class SidedInventoryDelegate(private val inventory: Inventory, private val side: Direction) : InventoryDelegate {
    override fun getIndices() = if (inventory is SidedInventory) inventory.getAvailableSlots(side).asIterable() else inventory.indices
    override fun getItemStack(index: Int) = inventory[index]
    override fun setItemStack(index: Int, itemStack: ItemStack) = unit { inventory[index] = itemStack }
    override fun canExtract(index: Int, itemStack: ItemStack) = if (inventory is SidedInventory) inventory.canExtract(index, itemStack, side) else true
    override fun canInsert(index: Int, itemStack: ItemStack) = (if (inventory is SidedInventory) inventory.canInsert(index, itemStack, side) else true) && inventory.isValid(index, itemStack)
    override fun getMaxCountPerStack(index: Int) = inventory.maxCountPerStack
    override fun markDirty() = inventory.markDirty()
}

fun Inventory.toSidedInventoryDelegate(side: Direction) = SidedInventoryDelegate(this, side)

fun InventoryDelegate.mergeTo(other: InventoryDelegate) = mergeInventory(this, other)

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun Inventory.mergeTo(other: Inventory) = mergeInventory(this.toInventoryDelegate(), other.toInventoryDelegate())


// Insert

interface InventoryAccessor {
    val size: Int
    fun getItemStack(index: Int): ItemStack
    fun setItemStack(index: Int, itemStack: ItemStack)
    fun getMaxItemCount(index: Int): Int
    fun canInsert(index: Int, itemStack: ItemStack): Boolean
    fun markDirty(index: Int)
}

val ScreenHandler.inventoryAccessor: InventoryAccessor
    get() = object : InventoryAccessor {
        override val size: Int get() = this@inventoryAccessor.slots.size
        override fun getItemStack(index: Int) = this@inventoryAccessor.slots[index].stack
        override fun setItemStack(index: Int, itemStack: ItemStack) = unit { this@inventoryAccessor.slots[index].stack = itemStack }
        override fun getMaxItemCount(index: Int) = this@inventoryAccessor.slots[index].maxItemCount
        override fun canInsert(index: Int, itemStack: ItemStack) = this@inventoryAccessor.slots[index].canInsert(itemStack)
        override fun markDirty(index: Int) = this@inventoryAccessor.slots[index].markDirty()
    }

val Inventory.inventoryAccessor: InventoryAccessor
    get() = object : InventoryAccessor {
        override val size: Int get() = this@inventoryAccessor.size
        override fun getItemStack(index: Int) = this@inventoryAccessor[index]
        override fun setItemStack(index: Int, itemStack: ItemStack) = unit { this@inventoryAccessor[index] = itemStack }
        override fun getMaxItemCount(index: Int) = this@inventoryAccessor.maxCountPerStack
        override fun canInsert(index: Int, itemStack: ItemStack) = this@inventoryAccessor.isValid(index, itemStack)
        override fun markDirty(index: Int) = this@inventoryAccessor.markDirty()
    }

/** @see ScreenHandler.insertItem */
fun InventoryAccessor.insertItem(insertItemStack: ItemStack, indices: Iterable<Int>): Boolean {
    var moved = false

    // 既存スロットへの挿入
    if (insertItemStack.isStackable) run {
        indices.forEach { i ->
            if (insertItemStack.isEmpty) return@run // 挿入完了
            val slotItemStack = this.getItemStack(i)
            if (slotItemStack.isNotEmpty && this.canInsert(i, insertItemStack) && ItemStack.canCombine(insertItemStack, slotItemStack)) { // 宛先が空でなく、そのアイテムを挿入可能で、既存アイテムとスタック可能な場合
                val moveCount = insertItemStack.count atMost (slotItemStack.maxCount - slotItemStack.count atLeast 0)
                if (moveCount > 0) {
                    insertItemStack.count -= moveCount
                    slotItemStack.count += moveCount
                    this.markDirty(i)
                    moved = true
                }
            }
        }
    }

    // 新規スロットへの挿入
    if (!insertItemStack.isEmpty) run {
        indices.forEach { i ->
            val slotItemStack = this.getItemStack(i)
            if (slotItemStack.isEmpty && this.canInsert(i, insertItemStack)) { // 宛先が空っぽかつ、そのアイテムを挿入可能な場合
                val moveCount = insertItemStack.count atMost this.getMaxItemCount(i)
                this.setItemStack(i, insertItemStack.split(moveCount))
                this.markDirty(i)
                moved = true
                return@run // 新規スロットへの挿入は一度に1スロット分しかしない
            }
        }
    }

    return moved
}


// MutableList

fun MutableList<ItemStack>.reset() = this.replaceAll { EMPTY_ITEM_STACK }

fun MutableList<ItemStack>.readFromNbt(nbt: NbtCompound) {
    nbt.wrapper["Items"].list.get()?.let { items ->
        items.forEach { item ->
            item.wrapper.compound.get()?.let { itemCompound ->
                val slotIndex = (itemCompound.wrapper["Slot"].byte.orDefault { 0 }.get() and 255.toByte()).toInt()
                if (slotIndex in this.indices) this[slotIndex] = ItemStack.fromNbt(itemCompound)
            }
        }
    }
}

fun List<ItemStack>.writeToNbt(nbt: NbtCompound) {
    val nbtList = NbtList()
    this.forEachIndexed { slotIndex, itemStack ->
        if (itemStack.isNotEmpty) {
            val itemCompound = NbtCompound()
            itemCompound.wrapper["Slot"].byte.set(slotIndex.toByte())
            itemStack.writeNbt(itemCompound)
            nbtList.add(itemCompound)
        }
    }
    nbt.put("Items", nbtList)
}

fun List<ItemStack>.writeToNbt() = NbtCompound().also { this.writeToNbt(it) }
