package miragefairy2024.lib

import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.reset
import miragefairy2024.util.writeToNbt
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

abstract class MachineBlockEntity<E : MachineBlockEntity<E>>(private val card: MachineCard<*, *, *>, blockEntityType: BlockEntityType<*>, pos: BlockPos, state: BlockState) : LockableContainerBlockEntity(blockEntityType, pos, state), SidedInventory {

    interface InventorySlotConfiguration {
        fun isValid(itemStack: ItemStack): Boolean
        fun canInsert(direction: Direction): Boolean
        fun canExtract(direction: Direction): Boolean
        val isObservable: Boolean
        val dropItem: Boolean
    }


    // Data

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        inventory.reset()
        inventory.readFromNbt(nbt)
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        inventory.writeToNbt(nbt)
    }

    override fun toInitialChunkDataNbt(): NbtCompound = createNbt()

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)


    // Inventory

    private val inventory = MutableList(card.inventorySlotConfigurations.size) { EMPTY_ITEM_STACK }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack = inventory.getOrElse(slot) { EMPTY_ITEM_STACK }

    override fun setStack(slot: Int, stack: ItemStack) {
        if (slot in inventory.indices) {
            inventory[slot] = stack
        }
        if (card.inventorySlotConfigurations[slot].isObservable) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (card.inventorySlotConfigurations[slot].isObservable) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        if (card.inventorySlotConfigurations[slot].isObservable) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.removeStack(inventory, slot)
    }

    override fun isValid(slot: Int, stack: ItemStack) = card.inventorySlotConfigurations[slot].isValid(stack)

    abstract fun getActualSide(side: Direction): Direction

    override fun getAvailableSlots(side: Direction) = card.availableInventorySlotsTable[getActualSide(side).id]

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = (dir == null || card.inventorySlotConfigurations[slot].canInsert(getActualSide(dir))) && isValid(slot, stack)

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction) = card.inventorySlotConfigurations[slot].canExtract(getActualSide(dir))

    override fun clear() {
        world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        inventory.replaceAll { EMPTY_ITEM_STACK }
    }

    fun dropItems() {
        inventory.forEachIndexed { index, itemStack ->
            if (card.inventorySlotConfigurations[index].dropItem) ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack)
        }
        world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }

}
