package miragefairy2024

import net.minecraft.item.ItemStack
import net.minecraft.text.Text

interface ClientProxy {
    fun registerItemTooltipCallback(block: (stack: ItemStack, lines: MutableList<Text>) -> Unit)
}