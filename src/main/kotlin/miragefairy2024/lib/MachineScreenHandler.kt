package miragefairy2024.lib

import miragefairy2024.util.quickMove
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text

open class MachineScreenHandler(private val card: MachineCard<*, *, *>, private val arguments: Arguments) : ScreenHandler(card.screenHandlerType, arguments.syncId) {

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

    interface PropertyConfiguration<in E> {
        fun get(blockEntity: E): Int
        fun set(blockEntity: E, value: Int)
        fun encode(value: Int): Short
        fun decode(data: Short): Int
    }

    class MachineSlot(val configuration: GuiSlotConfiguration, inventory: Inventory, index: Int) : Slot(inventory, index, configuration.x, configuration.y) {
        override fun canInsert(stack: ItemStack) = configuration.isValid(stack)
    }

    init {
        checkSize(arguments.inventory, card.guiSlotConfigurations.size)
        checkDataCount(arguments.propertyDelegate, card.propertyConfigurations.size)

        val y = card.guiHeight - 82
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(arguments.playerInventory, 9 + r * 9 + c, 8 + c * 18, y + r * 18))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(arguments.playerInventory, c, 8 + c * 18, y + 18 * 3 + 4))
        }
        card.guiSlotConfigurations.forEachIndexed { index, configuration ->
            addSlot(MachineSlot(configuration, arguments.inventory, index))
        }

        @Suppress("LeakingThis")
        addProperties(arguments.propertyDelegate)
    }

    fun getTooltip(slot: Slot): List<Text>? {
        if (slot.hasStack()) return null // アイテムのツールチップを優先
        if (slot !is MachineSlot) return null
        return slot.configuration.getTooltip()
    }

    override fun canUse(player: PlayerEntity) = arguments.inventory.canPlayerUse(player)

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        val playerIndices = 9 * 4 - 1 downTo 0
        val utilityIndices = 9 * 4 until slots.size
        val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
        return quickMove(slot, destinationIndices)
    }

    inner class Property(private val property: PropertyConfiguration<*>) {
        operator fun getValue(thisRef: Any?, property: Any?): Int {
            val propertyIndex = card.propertyIndexTable[this.property] ?: throw NullPointerException("No such property")
            return this.property.decode(arguments.propertyDelegate.get(propertyIndex).toShort())
        }

        operator fun setValue(thisRef: Any?, property: Any?, value: Int) {
            val propertyIndex = card.propertyIndexTable[this.property] ?: throw NullPointerException("No such property")
            arguments.propertyDelegate.set(propertyIndex, this.property.encode(value).toInt())
        }
    }

}
