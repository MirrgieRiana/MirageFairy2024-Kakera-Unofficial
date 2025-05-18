package miragefairy2024

import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.ItemEnchantments

var platformProxy: PlatformProxy? = null


interface PlatformProxy {
    fun registerModifyItemEnchantmentsHandler(handler: ModifyItemEnchantmentsHandler)
}

fun interface ModifyItemEnchantmentsHandler {
    fun modifyItemEnchantments(itemStack: ItemStack, mutableItemEnchantments: ItemEnchantments.Mutable, enchantmentLookup: HolderLookup.RegistryLookup<Enchantment>)
}
