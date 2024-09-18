package miragefairy2024.util

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack

fun Enchantment.getLevel(itemStack: ItemStack) = EnchantmentHelper.getLevel(this, itemStack)
fun Enchantment.getRate(itemStack: ItemStack) = this.getLevel(itemStack).toDouble() / this.maxLevel.toDouble()
