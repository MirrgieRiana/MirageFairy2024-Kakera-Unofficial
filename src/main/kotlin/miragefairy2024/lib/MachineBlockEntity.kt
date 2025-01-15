package miragefairy2024.lib

import miragefairy2024.RenderingProxy
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.reset
import miragefairy2024.util.writeToNbt
import mirrg.kotlin.hydrogen.unit
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class MachineBlockEntity<E : MachineBlockEntity<E>>(private val card: MachineCard<*, E, *>, pos: BlockPos, state: BlockState) : LockableContainerBlockEntity(card.blockEntityType, pos, state), SidedInventory {

    interface InventorySlotConfiguration {
        fun isValid(itemStack: ItemStack): Boolean
        fun canInsert(direction: Direction): Boolean
        fun canExtract(direction: Direction): Boolean
        val isObservable: Boolean
        val dropItem: Boolean
    }

    abstract fun getThis(): E


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


    // Move

    abstract fun serverTick(world: World, pos: BlockPos, state: BlockState)

    abstract fun clientTick(world: World, pos: BlockPos, state: BlockState)


    // Rendering

    interface Animation<in E> {
        fun tick(blockEntity: E)
        fun render(blockEntity: E, renderingProxy: RenderingProxy, tickDelta: Float)
    }


    // Gui

    private val propertyDelegate = object : PropertyDelegate {
        override fun size() = card.propertyConfigurations.size
        override fun get(index: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.encode(it.get(getThis())).toInt() } ?: 0
        override fun set(index: Int, value: Int) = unit { card.propertyConfigurations.getOrNull(index)?.let { it.set(getThis(), it.decode(value.toShort())) } }
    }

    override fun canPlayerUse(player: PlayerEntity) = Inventory.canPlayerUse(this, player)

    override fun getContainerName(): Text = card.block.name

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        val arguments = MachineScreenHandler.Arguments(
            syncId,
            playerInventory,
            this,
            propertyDelegate,
            ScreenHandlerContext.create(world, pos),
        )
        return card.createScreenHandler(arguments)
    }

}
