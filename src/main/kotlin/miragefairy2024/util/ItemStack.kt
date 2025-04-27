package miragefairy2024.util

import mirrg.kotlin.hydrogen.atMost
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag as NbtCompound

fun Item.createItemStack(count: Int = 1) = ItemStack(this, count atMost this.defaultMaxStackSize)

val EMPTY_ITEM_STACK: ItemStack get() = ItemStack.EMPTY
val ItemStack?.orEmpty get() = this ?: EMPTY_ITEM_STACK
val ItemStack.isNotEmpty get() = !this.isEmpty

fun ItemStack.toNbt(): NbtCompound = NbtCompound().also { this.save(it) }
fun NbtCompound.toItemStack(): ItemStack = ItemStack.of(this)

infix fun ItemStack.hasSameItem(other: ItemStack) = this.item == other.item
infix fun ItemStack.hasSameItemAndComponents(other: ItemStack) = this hasSameItem other && this.components == other.components
infix fun ItemStack.hasSameItemAndComponentsAndCount(other: ItemStack) = this hasSameItemAndComponents other && this.count == other.count

fun ItemStack.repair(amount: Int) {
    if (amount <= 0) return
    if (!this.isDamageableItem) return
    val actualAmount = amount atMost this.damageValue
    if (actualAmount <= 0) return
    this.damageValue -= actualAmount
}
