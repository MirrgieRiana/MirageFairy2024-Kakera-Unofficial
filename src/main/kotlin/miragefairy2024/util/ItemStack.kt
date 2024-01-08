package miragefairy2024.util

import mirrg.kotlin.hydrogen.atMost
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import java.util.Objects

fun Item.createItemStack(count: Int = 1) = ItemStack(this, count atMost this.maxCount)

val EMPTY_ITEM_STACK: ItemStack get() = ItemStack.EMPTY
val ItemStack?.orEmpty get() = this ?: EMPTY_ITEM_STACK
val ItemStack.isNotEmpty get() = !this.isEmpty

fun ItemStack.toNbt(): NbtCompound = NbtCompound().also { this.writeNbt(it) }
fun NbtCompound.toItemStack(): ItemStack = ItemStack.fromNbt(this)

infix fun ItemStack.hasSameItem(other: ItemStack) = this.item === other.item
infix fun ItemStack.hasSameItemAndNbt(other: ItemStack) = this hasSameItem other && Objects.equals(this.nbt, other.nbt)
infix fun ItemStack.hasSameItemAndNbtAndCount(other: ItemStack) = this hasSameItemAndNbt other && this.count == other.count
