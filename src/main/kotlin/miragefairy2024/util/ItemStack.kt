package miragefairy2024.util

import mirrg.kotlin.hydrogen.atMost
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

fun Item.createItemStack(count: Int = 1) = ItemStack(this, count atMost this.maxCount)

val EMPTY_ITEM_STACK: ItemStack get() = ItemStack.EMPTY
