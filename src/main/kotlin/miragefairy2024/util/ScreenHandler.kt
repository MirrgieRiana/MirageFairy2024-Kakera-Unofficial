package miragefairy2024.util

import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

fun ScreenHandler.quickMove(slot: Int, lowerIndices: Iterable<Int>, upperIndices: Iterable<Int>): ItemStack {
    if (slot < 0 || slot >= slots.size) return EMPTY_ITEM_STACK
    if (!slots[slot].hasStack()) return EMPTY_ITEM_STACK // そこに何も無い場合は何もしない

    val newItemStack = slots[slot].stack
    val originalItemStack = newItemStack.copy()

    if (slot in lowerIndices) { // 上へ
        if (!inventoryAccessor.insertItem(newItemStack, upperIndices)) return EMPTY_ITEM_STACK
    } else { // 下へ
        if (!inventoryAccessor.insertItem(newItemStack, lowerIndices)) return EMPTY_ITEM_STACK
    }
    slots[slot].onQuickTransfer(newItemStack, originalItemStack)

    // 終了処理
    if (newItemStack.isEmpty) {
        slots[slot].stack = EMPTY_ITEM_STACK
    } else {
        slots[slot].markDirty()
    }

    return originalItemStack
}
