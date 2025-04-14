package miragefairy2024.lib

import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.reset
import miragefairy2024.util.writeToNbt
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity as LockableContainerBlockEntity
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.world.ContainerHelper as Inventories
import net.minecraft.world.Container as Inventory
import net.minecraft.world.WorldlyContainer as SidedInventory
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.network.protocol.game.ClientGamePacketListener as ClientPlayPacketListener
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket as BlockEntityUpdateS2CPacket
import net.minecraft.world.inventory.ContainerData as PropertyDelegate
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext
import net.minecraft.network.chat.Component as Text
import net.minecraft.world.Containers as ItemScatterer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level as World

abstract class MachineBlockEntity<E : MachineBlockEntity<E>>(private val card: MachineCard<*, E, *>, pos: BlockPos, state: BlockState) : LockableContainerBlockEntity(card.blockEntityType, pos, state), SidedInventory, RenderingProxyBlockEntity {

    interface InventorySlotConfiguration {
        fun isValid(itemStack: ItemStack): Boolean
        fun canInsert(direction: Direction): Boolean
        fun canExtract(direction: Direction): Boolean
        val isObservable: Boolean
        val dropItem: Boolean
    }

    abstract fun getThis(): E


    // Data

    override fun load(nbt: NbtCompound) {
        super.load(nbt)
        inventory.reset()
        inventory.readFromNbt(nbt)
    }

    override fun saveAdditional(nbt: NbtCompound) {
        super.saveAdditional(nbt)
        inventory.writeToNbt(nbt)
    }

    override fun getUpdateTag(): NbtCompound = saveWithoutMetadata() // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない

    override fun getUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this) // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない


    // Inventory

    /**
     * スロットの内容が変化する際に呼び出されます。
     * このイベントはスロットの更新が行われた後に呼び出されることは保証されません。
     */
    open fun onStackChange(slot: Int?) {
        // TODO スロットアップデートのための軽量カスタムパケット
        if (slot == null || card.inventorySlotConfigurations[slot].isObservable) {
            world?.updateListeners(pos, cachedState, cachedState, Block.UPDATE_ALL)
        }
    }

    private val inventory = MutableList(card.inventorySlotConfigurations.size) { EMPTY_ITEM_STACK }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack = inventory.getOrElse(slot) { EMPTY_ITEM_STACK }

    override fun setStack(slot: Int, stack: ItemStack) {
        if (slot in inventory.indices) {
            inventory[slot] = stack
        }
        onStackChange(slot)
        markDirty()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        onStackChange(slot)
        markDirty()
        return Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        onStackChange(slot)
        markDirty()
        return Inventories.removeStack(inventory, slot)
    }

    override fun isValid(slot: Int, stack: ItemStack) = card.inventorySlotConfigurations[slot].isValid(stack)

    abstract fun getActualSide(side: Direction): Direction

    override fun getAvailableSlots(side: Direction) = card.availableInventorySlotsTable[getActualSide(side).id]

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = (dir == null || card.inventorySlotConfigurations[slot].canInsert(getActualSide(dir))) && isValid(slot, stack)

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction) = card.inventorySlotConfigurations[slot].canExtract(getActualSide(dir))

    override fun clear() {
        onStackChange(null)
        markDirty()
        inventory.replaceAll { EMPTY_ITEM_STACK }
    }

    open fun dropItems() {
        inventory.forEachIndexed { index, itemStack ->
            if (card.inventorySlotConfigurations[index].dropItem) ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack)
        }
        onStackChange(null)
        markDirty()
    }


    // Move

    open fun serverTick(world: World, pos: BlockPos, state: BlockState) = Unit


    // Rendering

    interface AnimationConfiguration<in E> {
        fun createAnimation(): Animation<E>?
    }

    interface Animation<in E> {
        fun tick(blockEntity: E)
        fun render(blockEntity: E, renderingProxy: RenderingProxy, tickDelta: Float)
    }

    private val animations = card.animationConfigurations.mapNotNull { it.createAnimation() }

    open fun clientTick(world: World, pos: BlockPos, state: BlockState) {
        animations.forEach {
            it.tick(getThis())
        }
    }

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        renderRotated(renderingProxy, tickDelta, light, overlay)
    }

    open fun renderRotated(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        animations.forEach {
            it.render(getThis(), renderingProxy, tickDelta)
        }
    }


    // Gui

    private val propertyDelegate = object : PropertyDelegate {
        override fun size() = card.propertyConfigurations.size
        override fun get(index: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.encode(it.get(getThis())).toInt() } ?: 0
        override fun set(index: Int, value: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.set(getThis(), it.decode(value.toShort())) } ?: Unit
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
