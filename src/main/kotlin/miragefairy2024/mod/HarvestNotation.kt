package miragefairy2024.mod

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.createItemStack
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

val harvestNotations = mutableListOf<HarvestNotation>()

class HarvestNotation(val seed: ItemStack, val crops: List<ItemStack>)

context(ModContext)
fun Item.registerHarvestNotation(vararg drops: Item) = this.registerHarvestNotation(drops.asIterable())

context(ModContext)
fun Item.registerHarvestNotation(drops: Iterable<Item>) = ModEvents.onInitialize {
    harvestNotations += HarvestNotation(this.createItemStack(), drops.map { it.createItemStack() })
}
