package miragefairy2024.lib

import io.netty.buffer.ByteBuf
import miragefairy2024.util.FilteringSlot
import miragefairy2024.util.quickMove
import mirrg.kotlin.hydrogen.unit
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.Slot

abstract class RichMachineScreenHandler(val arguments: Arguments) : ScreenHandler(arguments.configuration.type, arguments.syncId) {

    class Arguments(
        val configuration: Configuration,
        val syncId: Int,
        val playerInventory: PlayerInventory,
        val machineInventory: Inventory, // TODO プレイヤースロットも包含
        val properties: List<Property>,
        val context: ScreenHandlerContext,
    )

    interface Configuration {
        val type: ScreenHandlerType<*>
        val width: Int
        val height: Int
        val machineSlotConfigurations: List<SlotConfiguration> // TODO プレイヤースロットも包含
        val propertyConfigurations: List<PropertyConfiguration>
    }

    interface SlotConfiguration {
        val x: Int
        val y: Int
    }

    interface PropertyConfiguration {
        fun encode(value: Int): Short = value.toShort()
        fun decode(data: Short): Int = data.toInt()
    }

    interface Property {
        fun get(): Int
        fun set(value: Int)
    }

    private val propertyDelegate = object : PropertyDelegate {
        override fun get(index: Int): Int {
            if (index < 0 || index >= arguments.properties.size) return 0
            return arguments.configuration.propertyConfigurations[index].encode(arguments.properties[index].get()).toInt()
        }

        override fun set(index: Int, value: Int) {
            if (index < 0 || index >= arguments.properties.size) return
            arguments.properties[index].set(arguments.configuration.propertyConfigurations[index].decode(value.toShort()))
        }

        override fun size() = arguments.properties.size
    }

    init {
        checkSize(arguments.machineInventory, arguments.configuration.machineSlotConfigurations.size)
        checkDataCount(propertyDelegate, arguments.configuration.propertyConfigurations.size)

        // TODO PlayerInventoryのカスタマイズ
        val y = arguments.configuration.height - 82 // TODO configuration化
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(arguments.playerInventory, 9 + r * 9 + c, 8 + c * 18, y + r * 18))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(arguments.playerInventory, c, 8 + c * 18, y + 18 * 3 + 4))
        }
        arguments.configuration.machineSlotConfigurations.forEachIndexed { index, slotConfiguration ->
            addSlot(FilteringSlot(arguments.machineInventory, index, slotConfiguration.x, slotConfiguration.y))
        }

        @Suppress("LeakingThis")
        addProperties(propertyDelegate)
    }

    override fun quickMove(player: PlayerEntity, slot: Int) = quickMove(slot, 9 * 4 - 1 downTo 0, 9 * 4 until slots.size)

}

fun <H : ScreenHandler> RichMachineScreenHandler.Configuration.createScreenHandlerType(screenHandlerCreator: (arguments: RichMachineScreenHandler.Arguments, buf: ByteBuf) -> H): ExtendedScreenHandlerType<H> {
    return ExtendedScreenHandlerType { syncId, playerInventory, buf ->
        val arguments = RichMachineScreenHandler.Arguments(
            this,
            syncId,
            playerInventory,
            SimpleInventory(this.machineSlotConfigurations.size),
            (0 until this.propertyConfigurations.size).map {
                object : RichMachineScreenHandler.Property {
                    private var value = 0
                    override fun get() = value
                    override fun set(value: Int) = unit { this.value = value }
                }
            },
            ScreenHandlerContext.EMPTY,
        )
        screenHandlerCreator(arguments, buf)
    }
}


class SimpleMachineScreenHandlerDelegate(private val screenHandler: RichMachineScreenHandler, private val propertyConfiguration: RichMachineScreenHandler.PropertyConfiguration) {
    private val index = screenHandler.arguments.configuration.propertyConfigurations.indexOf(propertyConfiguration)

    init {
        check(index >= 0) { "No such property" }
    }

    operator fun getValue(thisRef: Any?, property: Any?): Int = propertyConfiguration.decode(screenHandler.arguments.properties[index].get().toShort())
    operator fun setValue(thisRef: Any?, property: Any?, value: Int) = screenHandler.arguments.properties[index].set(propertyConfiguration.encode(value).toInt())
}

context(RichMachineScreenHandler)
val RichMachineScreenHandler.PropertyConfiguration.delegate
    get() = SimpleMachineScreenHandlerDelegate(this@RichMachineScreenHandler, this)
