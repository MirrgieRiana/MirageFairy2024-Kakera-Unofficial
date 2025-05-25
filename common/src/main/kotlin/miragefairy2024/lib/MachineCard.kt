package miragefairy2024.lib

import miragefairy2024.ModContext
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.Registration
import miragefairy2024.util.dummyUnitStreamCodec
import miragefairy2024.util.register
import miragefairy2024.util.times
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.SimpleContainer as SimpleInventory
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext
import net.minecraft.world.inventory.SimpleContainerData as ArrayPropertyDelegate

@Suppress("LeakingThis") // ブートストラップ問題のため解決不可能なので妥協する
abstract class MachineCard<B : Block, E : MachineBlockEntity<E>, H : MachineScreenHandler> {
    companion object {
        context(C)
        inline fun <C, reified E> BlockEntityAccessor(crossinline creator: (card: C, blockPos: BlockPos, blockState: BlockState) -> E) = object : BlockEntityAccessor<E> {
            override fun create(blockPos: BlockPos, blockState: BlockState) = creator(this@C, blockPos, blockState)
            override fun castOrThrow(blockEntity: BlockEntity?) = blockEntity as E
            override fun castOrNull(blockEntity: BlockEntity?) = blockEntity as? E
        }
    }


    // Specification

    abstract fun createIdentifier(): ResourceLocation
    val identifier = createIdentifier()


    // Block

    abstract fun createBlockSettings(): FabricBlockSettings
    abstract fun createBlock(): B
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { createBlock() }


    // BlockEntity

    val inventorySlotConfigurations = mutableListOf<MachineBlockEntity.InventorySlotConfiguration>()

    val inventorySlotIndexTable by lazy {
        inventorySlotConfigurations.withIndex().associate { (index, it) -> it to index }
    }

    val availableInventorySlotsTable by lazy {
        Direction.entries.map { direction ->
            inventorySlotConfigurations.withIndex()
                .filter { it.value.canInsert(direction) || it.value.canExtract(direction) }
                .map { it.index }
                .toIntArray()
        }.toTypedArray()
    }

    val animationConfigurations = mutableListOf<MachineBlockEntity.AnimationConfiguration<E>>()

    interface BlockEntityAccessor<E> {
        fun create(blockPos: BlockPos, blockState: BlockState): E
        fun castOrThrow(blockEntity: BlockEntity?): E
        fun castOrNull(blockEntity: BlockEntity?): E?
    }

    abstract fun createBlockEntityAccessor(): BlockEntityAccessor<E>
    val blockEntityAccessor = createBlockEntityAccessor()
    val blockEntityType = Registration(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier) { BlockEntityType(blockEntityAccessor::create, setOf(block.await()), null) }


    // Item

    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }


    // ScreenHandler

    abstract fun createScreenHandler(arguments: MachineScreenHandler.Arguments): H
    val screenHandlerType = Registration(BuiltInRegistries.MENU, identifier) {
        ExtendedScreenHandlerType({ syncId, playerInventory, _ ->
            val arguments = MachineScreenHandler.Arguments(
                syncId,
                playerInventory,
                SimpleInventory(inventorySlotConfigurations.size),
                ArrayPropertyDelegate(propertyConfigurations.size),
                ScreenHandlerContext.NULL,
            )
            createScreenHandler(arguments)
        }, dummyUnitStreamCodec())
    }


    // Gui

    abstract val guiWidth: Int
    abstract val guiHeight: Int

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"

    val guiSlotConfigurations = mutableListOf<MachineScreenHandler.GuiSlotConfiguration>()

    val guiSlotIndexTable by lazy {
        inventorySlotConfigurations.withIndex().associate { (index, it) -> it to 9 * 4 + index } // TODO プレイヤーインベントリの扱い
    }

    val propertyConfigurations = mutableListOf<MachineScreenHandler.PropertyConfiguration<E>>()

    val propertyIndexTable by lazy {
        propertyConfigurations.withIndex().associate { (index, it) -> it to index }
    }


    // Advancement

    open fun createAdvancement(): AdvancementCard? = null
    val advancement = createAdvancement()


    context(ModContext)
    open fun init() {
        block.register()
        blockEntityType.register()
        item.register()
        screenHandlerType.register()
        advancement?.init()
    }
}
