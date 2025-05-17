package miragefairy2024.lib

import miragefairy2024.util.quickMove
import net.minecraft.network.chat.Component
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.ContainerData as PropertyDelegate
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext

open class MachineScreenHandler(private val card: MachineCard<*, *, *>, private val arguments: Arguments) : ScreenHandler(card.screenHandlerType(), arguments.syncId) {

    class Arguments(
        val syncId: Int,
        val playerInventory: Inventory,
        val inventory: Container,
        val propertyDelegate: PropertyDelegate,
        val context: ScreenHandlerContext,
    )

    interface GuiSlotConfiguration {
        val x: Int
        val y: Int
        fun isValid(itemStack: ItemStack): Boolean
        fun getTooltip(): List<Component>?
    }

    interface PropertyConfiguration<in E> {
        fun get(blockEntity: E): Int
        fun set(blockEntity: E, value: Int)
        fun encode(value: Int): Short
        fun decode(data: Short): Int
    }

    class MachineSlot(val configuration: GuiSlotConfiguration, inventory: Container, index: Int) : Slot(inventory, index, configuration.x, configuration.y) {
        override fun mayPlace(stack: ItemStack) = configuration.isValid(stack)
    }

    init {
        checkContainerSize(arguments.inventory, card.guiSlotConfigurations.size)
        checkContainerDataCount(arguments.propertyDelegate, card.propertyConfigurations.size)

        // TODO PlayerInventoryのカスタマイズ
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
        addDataSlots(arguments.propertyDelegate)
    }

    fun getTooltip(slot: Slot): List<Component>? {
        if (slot.hasItem()) return null // アイテムのツールチップを優先
        if (slot !is MachineSlot) return null
        return slot.configuration.getTooltip()
    }

    override fun stillValid(player: PlayerEntity) = arguments.inventory.stillValid(player)

    override fun quickMoveStack(player: PlayerEntity, slot: Int): ItemStack {
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
