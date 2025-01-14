package miragefairy2024.lib

import miragefairy2024.ModContext
import miragefairy2024.util.register
import miragefairy2024.util.times
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

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

    abstract fun createIdentifier(): Identifier
    val identifier = createIdentifier()


    // Block

    abstract fun createBlockSettings(): FabricBlockSettings
    abstract fun createBlock(): B
    val block = createBlock()


    // BlockEntity

    interface BlockEntityAccessor<E> {
        fun create(blockPos: BlockPos, blockState: BlockState): E
        fun castOrThrow(blockEntity: BlockEntity?): E
        fun castOrNull(blockEntity: BlockEntity?): E?
    }

    abstract fun createBlockEntityAccessor(): BlockEntityAccessor<E>
    val blockEntityAccessor = createBlockEntityAccessor()
    val blockEntityType = BlockEntityType(blockEntityAccessor::create, setOf(block), null)


    // Item

    val item = BlockItem(block, Item.Settings())


    // ScreenHandler

    abstract fun getSlotCount(): Int
    abstract fun getPropertyCount(): Int
    abstract fun createScreenHandler(arguments: MachineScreenHandler.Arguments): H
    val screenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
        val arguments = MachineScreenHandler.Arguments(
            syncId,
            playerInventory,
            SimpleInventory(getSlotCount()),
            ArrayPropertyDelegate(getPropertyCount()),
            ScreenHandlerContext.EMPTY,
        )
        createScreenHandler(arguments)
    }


    // Gui

    abstract val guiWidth: Int
    abstract val guiHeight: Int

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"


    context(ModContext)
    open fun init() {
        block.register(Registries.BLOCK, identifier)
        blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, identifier)
        item.register(Registries.ITEM, identifier)
        screenHandlerType.register(Registries.SCREEN_HANDLER, identifier)
    }
}
