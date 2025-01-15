package miragefairy2024.lib

import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text

abstract class MachineScreenHandler(private val card: MachineCard<*, *, *>, arguments: Arguments) : ScreenHandler(card.screenHandlerType, arguments.syncId) {

    class Arguments(
        val syncId: Int,
        val playerInventory: PlayerInventory,
        val inventory: Inventory,
        val propertyDelegate: PropertyDelegate,
        val context: ScreenHandlerContext,
    )

    interface GuiSlotConfiguration {
        val x: Int
        val y: Int
        fun isValid(itemStack: ItemStack): Boolean
        fun getTooltip(): List<Text>?
    }

    abstract fun getTooltip(slot: Slot): List<Text>?

}
