package miragefairy2024.util

import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler

fun ScreenHandler.quickMove(slotIndex: Int, destinationIndices: Iterable<Int>): ItemStack {
    if (slotIndex < 0 || slotIndex >= slots.size) return EMPTY_ITEM_STACK
    if (!slots[slotIndex].hasItem()) return EMPTY_ITEM_STACK // そこに何も無い場合は何もしない

    val newItemStack = slots[slotIndex].item
    val originalItemStack = newItemStack.copy()

    if (!inventoryAccessor.insertItem(newItemStack, destinationIndices)) return EMPTY_ITEM_STACK
    slots[slotIndex].onQuickTransfer(newItemStack, originalItemStack)

    // 終了処理
    if (newItemStack.isEmpty) {
        slots[slotIndex].set(EMPTY_ITEM_STACK)
    } else {
        slots[slotIndex].setChanged()
    }

    return originalItemStack
}
