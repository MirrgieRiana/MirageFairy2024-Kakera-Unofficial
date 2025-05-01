package miragefairy2024.lib

import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.reset
import miragefairy2024.util.writeToNbt
import mirrg.kotlin.hydrogen.unit
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.network.protocol.game.ClientGamePacketListener as ClientPlayPacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket as BlockEntityUpdateS2CPacket
import net.minecraft.world.ContainerHelper as Inventories
import net.minecraft.world.Containers as ItemScatterer
import net.minecraft.world.WorldlyContainer as SidedInventory
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.ContainerData as PropertyDelegate
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity as LockableContainerBlockEntity

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

    override fun loadAdditional(nbt: NbtCompound, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        inventory.reset()
        inventory.readFromNbt(nbt, registries)
    }

    override fun saveAdditional(nbt: NbtCompound, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        inventory.writeToNbt(nbt, registries)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): NbtCompound = saveWithoutMetadata(registries) // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない

    override fun getUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this) // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない


    // Container

    /**
     * スロットの内容が変化する際に呼び出されます。
     * このイベントはスロットの更新が行われた後に呼び出されることは保証されません。
     */
    open fun onStackChange(slot: Int?) {
        // TODO スロットアップデートのための軽量カスタムパケット
        if (slot == null || card.inventorySlotConfigurations[slot].isObservable) {
            level?.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_ALL)
        }
    }

    private var inventory: NonNullList<ItemStack> = NonNullList.withSize(card.inventorySlotConfigurations.size, EMPTY_ITEM_STACK)

    override fun getContainerSize() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getItem(slot: Int): ItemStack = inventory.getOrElse(slot) { EMPTY_ITEM_STACK }

    override fun getItems() = inventory

    override fun setItem(slot: Int, stack: ItemStack) {
        if (slot in inventory.indices) {
            inventory[slot] = stack
        }
        onStackChange(slot)
        setChanged()
    }

    override fun setItems(items: NonNullList<ItemStack>) = unit { inventory = items }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        onStackChange(slot)
        setChanged()
        return Inventories.removeItem(inventory, slot, amount)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        onStackChange(slot)
        setChanged()
        return Inventories.takeItem(inventory, slot)
    }

    override fun canPlaceItem(slot: Int, stack: ItemStack) = card.inventorySlotConfigurations[slot].isValid(stack)

    abstract fun getActualSide(side: Direction): Direction

    override fun getSlotsForFace(side: Direction) = card.availableInventorySlotsTable[getActualSide(side).get3DDataValue()]

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, dir: Direction?) = (dir == null || card.inventorySlotConfigurations[slot].canInsert(getActualSide(dir))) && canPlaceItem(slot, stack)

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, dir: Direction) = card.inventorySlotConfigurations[slot].canExtract(getActualSide(dir))

    override fun clearContent() {
        onStackChange(null)
        setChanged()
        inventory.replaceAll { EMPTY_ITEM_STACK }
    }

    open fun dropItems() {
        inventory.forEachIndexed { index, itemStack ->
            if (card.inventorySlotConfigurations[index].dropItem) ItemScatterer.dropItemStack(level, worldPosition.x.toDouble(), worldPosition.y.toDouble(), worldPosition.z.toDouble(), itemStack)
        }
        onStackChange(null)
        setChanged()
    }


    // Move

    open fun serverTick(world: Level, pos: BlockPos, state: BlockState) = Unit


    // Rendering

    interface AnimationConfiguration<in E> {
        fun createAnimation(): Animation<E>?
    }

    interface Animation<in E> {
        fun tick(blockEntity: E)
        fun render(blockEntity: E, renderingProxy: RenderingProxy, tickDelta: Float)
    }

    private val animations = card.animationConfigurations.mapNotNull { it.createAnimation() }

    open fun clientTick(world: Level, pos: BlockPos, state: BlockState) {
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
        override fun getCount() = card.propertyConfigurations.size
        override fun get(index: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.encode(it.get(getThis())).toInt() } ?: 0
        override fun set(index: Int, value: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.set(getThis(), it.decode(value.toShort())) } ?: Unit
    }

    override fun stillValid(player: PlayerEntity) = Container.stillValidBlockEntity(this, player)

    override fun getDefaultName(): Component = card.block.name

    override fun createMenu(syncId: Int, playerInventory: Inventory): ScreenHandler {
        val arguments = MachineScreenHandler.Arguments(
            syncId,
            playerInventory,
            this,
            propertyDelegate,
            ScreenHandlerContext.create(level, worldPosition),
        )
        return card.createScreenHandler(arguments)
    }

}
