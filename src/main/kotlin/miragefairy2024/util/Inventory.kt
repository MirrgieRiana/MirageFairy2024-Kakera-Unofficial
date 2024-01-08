package miragefairy2024.util

import mirrg.kotlin.hydrogen.atMost
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

operator fun Inventory.get(slot: Int): ItemStack = this.getStack(slot)
operator fun Inventory.set(slot: Int, stack: ItemStack) = this.setStack(slot, stack)
val Inventory.size get() = this.size()
val Inventory.indices get() = 0 until this.size

// Merge

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(srcInventory: Inventory, srcSlotIndex: Int, destInventory: Inventory, destSlotIndex: Int): Boolean {
    when {
        srcInventory[srcSlotIndex].isEmpty -> return true // 元から空なので何もする必要はない
        !destInventory.isValid(destSlotIndex, srcInventory[srcSlotIndex]) -> return false // 宛先にこの種類のアイテムが入らない
        destInventory[destSlotIndex].isEmpty || srcInventory[srcSlotIndex] hasSameItemAndNbt destInventory[destSlotIndex] -> {
            // 先が空もしくは元と同じ種類のアイテムが入っている場合、マージ

            // 個数計算
            val allCount = srcInventory[srcSlotIndex].count + destInventory[destSlotIndex].count
            val destCount = allCount atMost destInventory.maxCountPerStack atMost srcInventory[srcSlotIndex].maxCount

            // 移動処理
            if (destInventory[destSlotIndex].isEmpty) {
                destInventory[destSlotIndex] = srcInventory[srcSlotIndex].copyWithCount(destCount)
            } else {
                destInventory[destSlotIndex].count = destCount
            }
            srcInventory[srcSlotIndex].count = allCount - destCount

            return srcInventory[srcSlotIndex].isEmpty
        }

        else -> return false // 宛先に別のアイテムが入っているので何もできない
    }
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(srcInventory: Inventory, srcIndex: Int, destInventory: Inventory, destIndices: Iterable<Int>): Boolean {
    destIndices.forEach { destIndex ->
        if (mergeInventory(srcInventory, srcIndex, destInventory, destIndex)) return true // すべてマージされたのでこれ以降の判定は不要
    }
    return false
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun mergeInventory(srcInventory: Inventory, srcIndices: Iterable<Int>, destInventory: Inventory, destIndices: Iterable<Int>): Boolean {
    return srcIndices.all { srcIndex -> mergeInventory(srcInventory, srcIndex, destInventory, destIndices) }
}

/**
 * インベントリのアイテムを別のインベントリに可能な限り移動させる
 * @return すべてのアイテムが完全に移動したかどうか
 */
fun Inventory.mergeTo(other: Inventory) = mergeInventory(this, this.indices, other, other.indices)
