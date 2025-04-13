package miragefairy2024.util

import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.ItemStack

fun Enchantment.getLevel(itemStack: ItemStack) = EnchantmentHelper.getLevel(this, itemStack)
fun Enchantment.getRate(itemStack: ItemStack) = this.getLevel(itemStack).toDouble() / this.maxLevel.toDouble()
