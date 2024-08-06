package miragefairy2024.mod.magicplant

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.createItemStack
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

val harvestNotations = mutableListOf<HarvestNotation>()

class HarvestNotation(val seed: ItemStack, val crops: List<ItemStack>)

context(ModContext)
fun registerMagicPlantDropNotation(seed: Item, vararg drops: Item) = ModEvents.onInitialize {
    harvestNotations += HarvestNotation(seed.createItemStack(), drops.map { it.createItemStack() })
}
