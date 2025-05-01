package miragefairy2024.util

import net.minecraft.core.Holder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper

fun Holder<Enchantment>.getLevel(itemStack: ItemStack) = EnchantmentHelper.getItemEnchantmentLevel(this, itemStack)
fun Holder<Enchantment>.getRate(itemStack: ItemStack) = this.getLevel(itemStack).toDouble() / this.value().maxLevel.toDouble()
