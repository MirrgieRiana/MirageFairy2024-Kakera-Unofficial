package miragefairy2024.mod.materials.contents

import miragefairy2024.util.createItemStack
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class ApostleWandItem(settings: Properties) : Item(settings) {
    override fun hasCraftingRemainingItem() = true
    override fun getRecipeRemainder(stack: ItemStack) = stack.item.createItemStack()
}
