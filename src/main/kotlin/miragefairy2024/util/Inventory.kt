package miragefairy2024.util

import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ScreenHandler
import kotlin.experimental.and

operator fun Inventory.get(slot: Int): ItemStack = this.getStack(slot)
operator fun Inventory.set(slot: Int, stack: ItemStack) = this.setStack(slot, stack)
val Inventory.size get() = this.size()
val Inventory.indices get() = 0 until this.size
val Inventory.itemStacks get() = this.indices.map { this[it] }

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
fun mergeInventory(srcInventory: Inventory, srcSlotIndex: Int, destInventory: Inventory, destSlotIndex: Int): MergeResult {

    val srcItemStack = srcInventory[srcSlotIndex]
    if (srcItemStack.isEmpty) return MergeResult.UP_TO_DATE // 元から空なので何もする必要はない

    if (!destInventory.isValid(destSlotIndex, srcItemStack)) return MergeResult.FAILED // 宛先にこの種類のアイテムが入らない

    val destItemStack = destInventory[destSlotIndex]
    if (destItemStack.isNotEmpty && !(srcItemStack hasSameItemAndNbt destItemStack)) return MergeResult.FAILED // 宛先に別のアイテムが入っているので何もできない

    // 先が空もしくは元と同じ種類のアイテムが入っているのでマージ

    // 個数計算
    val srcCount = srcItemStack.count
    val oldDestCount = destItemStack.count
    val allCount = srcCount + oldDestCount
    val newDestCount = allCount atMost destInventory.maxCountPerStack atMost srcItemStack.maxCount atLeast oldDestCount
    val moveCount = newDestCount - oldDestCount

    if (moveCount == 0) return MergeResult.FAILED // 宛先にこれ以上入らない

    // 移動処理
    if (destItemStack.isEmpty) {
        destInventory[destSlotIndex] = srcItemStack.copyWithCount(newDestCount)
    } else {
        destItemStack.count = newDestCount
    }
    val newSrcCount = allCount - newDestCount
    srcItemStack.count = newSrcCount
    if (newSrcCount == 0) srcInventory[srcSlotIndex] = EMPTY_ITEM_STACK

    return MergeResult(newSrcCount == 0, 1, moveCount)
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(srcInventory: Inventory, srcIndex: Int, destInventory: Inventory, destIndices: Iterable<Int>): MergeResult {
    var movementTimes = 0
    var movedItemCount = 0
    destIndices.forEach { destIndex ->
        val result = mergeInventory(srcInventory, srcIndex, destInventory, destIndex)
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
fun mergeInventory(srcInventory: Inventory, srcIndices: Iterable<Int>, destInventory: Inventory, destIndices: Iterable<Int>): MergeResult {
    var completed = true
    var movementTimes = 0
    var movedItemCount = 0
    srcIndices.forEach { srcIndex ->
        val result = mergeInventory(srcInventory, srcIndex, destInventory, destIndices)
        if (!result.completed) completed = false
        movementTimes += result.movementTimes
        movedItemCount += result.movedItemCount
    }
    return MergeResult(completed, movementTimes, movedItemCount)
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun Inventory.mergeTo(other: Inventory) = mergeInventory(this, this.indices, other, other.indices)


// Insert

/** @see ScreenHandler.insertItem */
fun ScreenHandler.insertItem(insertItemStack: ItemStack, indices: Iterable<Int>): Boolean {
    var moved = false

    // 既存スロットへの挿入
    if (insertItemStack.isStackable) run {
        indices.forEach { i ->
            if (insertItemStack.isEmpty) return@run // 挿入完了
            val slot = this.slots[i]
            val slotItemStack = slot.stack
            if (slotItemStack.isNotEmpty && slot.canInsert(insertItemStack) && ItemStack.canCombine(insertItemStack, slotItemStack)) { // 宛先が空でなく、そのアイテムを挿入可能で、既存アイテムとスタック可能な場合
                val moveCount = insertItemStack.count atMost (slotItemStack.maxCount - slotItemStack.count atLeast 0)
                if (moveCount > 0) {
                    insertItemStack.count -= moveCount
                    slotItemStack.count += moveCount
                    slot.markDirty()
                    moved = true
                }
            }
        }
    }

    // 新規スロットへの挿入
    if (!insertItemStack.isEmpty) run {
        indices.forEach { i ->
            val slot = this.slots[i]
            val slotItemStack = slot.stack
            if (slotItemStack.isEmpty && slot.canInsert(insertItemStack)) { // 宛先が空っぽかつ、そのアイテムを挿入可能な場合
                val moveCount = insertItemStack.count atMost slot.maxItemCount
                slot.stack = insertItemStack.split(moveCount)
                slot.markDirty()
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
