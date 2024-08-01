package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.BlockStateVariantRotation
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.checkType
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.getOrNull
import miragefairy2024.util.insertItem
import miragefairy2024.util.inventoryAccessor
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.reset
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.writeToNbt
import mirrg.kotlin.hydrogen.unit
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.block.enums.Instrument
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

abstract class FairyBuildingSettings<E : FairyBuildingBlockEntity<E>, H : FairyBuildingScreenHandler> {
    companion object {
        inline fun <reified E : FairyBuildingBlockEntity<E>> BlockEntityAccessor(crossinline creator: (blockPos: BlockPos, blockState: BlockState) -> E) = object : BlockEntityAccessor<E> {
            override fun create(blockPos: BlockPos, blockState: BlockState) = creator(blockPos, blockState)
            override fun castOrThrow(blockEntity: BlockEntity?) = blockEntity as E
            override fun castOrNull(blockEntity: BlockEntity?) = blockEntity as? E
        }
    }


    abstract val path: String
    abstract val tier: Int
    abstract val enName: String
    abstract val jaName: String
    abstract val enPoem: String
    abstract val jaPoem: String


    open fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().nonOpaque().strength(2.0F).instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.RAW_IRON_PINK)

    abstract fun createBlock(settings: FabricBlockSettings): FairyBuildingBlock


    abstract fun createBlockEntityAccessor(): BlockEntityAccessor<E>

    interface BlockEntityAccessor<E : FairyBuildingBlockEntity<E>> {
        fun create(blockPos: BlockPos, blockState: BlockState): E
        fun castOrThrow(blockEntity: BlockEntity?): E
        fun castOrNull(blockEntity: BlockEntity?): E?
    }


    abstract fun createScreenHandler(arguments: FairyBuildingScreenHandler.Arguments): H


    abstract val guiWidth: Int
    abstract val guiHeight: Int


    open fun createSlots(): List<SlotSettings> = listOf()

    class SlotSettings(
        val x: Int,
        val y: Int,
        val dropItem: Boolean = true,
        val insertDirections: Set<Direction> = setOf(),
        val extractDirections: Set<Direction> = setOf(),
        val appearance: Appearance? = null,
        val toolTipGetter: (() -> List<Text>)? = null,
        val filter: (ItemStack) -> Boolean = { true },
    )

    /**
     * @param x 1/16 scale
     * @param y 1/16 scale
     * @param z 1/16 scale
     * @param pitch degree
     * @param yaw degree
     */
    class Appearance(val x: Double, val y: Double, val z: Double, val pitch: Double, val yaw: Double)


    open fun createProperties(): List<PropertySettings<E>> = listOf()

    class PropertySettings<in E : FairyBuildingBlockEntity<*>>(
        val getter: E.() -> Int,
        val setter: E.(Int) -> Unit,
        val encoder: (Int) -> Short = { it.toShort() },
        val decoder: (Short) -> Int = { it.toInt() },
    )

}

open class FairyBuildingCard<S : FairyBuildingSettings<E, H>, E : FairyBuildingBlockEntity<E>, H : FairyBuildingScreenHandler>(val settings: S) {
    val identifier = Identifier(MirageFairy2024.modId, settings.path)

    val block = settings.createBlock(settings.createBlockSettings())

    val blockEntityAccessor = settings.createBlockEntityAccessor()
    val blockEntityType = BlockEntityType(blockEntityAccessor::create, setOf(block), null)

    val item = BlockItem(block, Item.Settings())

    val screenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
        val arguments = FairyBuildingScreenHandler.Arguments(
            syncId,
            playerInventory,
            SimpleInventory(slots.size),
            ArrayPropertyDelegate(properties.size),
            ScreenHandlerContext.EMPTY,
        )
        settings.createScreenHandler(arguments)
    }

    val slots = settings.createSlots()
    val availableSlotsTable = arrayOf(
        slots.withIndex().filter { Direction.DOWN in it.value.insertDirections || Direction.DOWN in it.value.extractDirections }.map { it.index }.toIntArray(),
        slots.withIndex().filter { Direction.UP in it.value.insertDirections || Direction.UP in it.value.extractDirections }.map { it.index }.toIntArray(),
        slots.withIndex().filter { Direction.NORTH in it.value.insertDirections || Direction.NORTH in it.value.extractDirections }.map { it.index }.toIntArray(),
        slots.withIndex().filter { Direction.SOUTH in it.value.insertDirections || Direction.SOUTH in it.value.extractDirections }.map { it.index }.toIntArray(),
        slots.withIndex().filter { Direction.WEST in it.value.insertDirections || Direction.WEST in it.value.extractDirections }.map { it.index }.toIntArray(),
        slots.withIndex().filter { Direction.EAST in it.value.insertDirections || Direction.EAST in it.value.extractDirections }.map { it.index }.toIntArray(),
    )

    val properties = settings.createProperties()
    val propertyIndexTable = properties.withIndex().associate { (index, it) -> it to index }

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"

    context(ModContext)
    open fun init() {

        block.register(Registries.BLOCK, identifier)
        blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, identifier)
        item.register(Registries.ITEM, identifier)
        screenHandlerType.register(Registries.SCREEN_HANDLER, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        block.registerVariantsBlockStateGeneration {
            val normal = BlockStateVariant(model = "block/" * block.getIdentifier())
            listOf(
                propertiesOf(HorizontalFacingBlock.FACING with Direction.NORTH) to normal.with(y = BlockStateVariantRotation.R0),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.EAST) to normal.with(y = BlockStateVariantRotation.R90),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.SOUTH) to normal.with(y = BlockStateVariantRotation.R180),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.WEST) to normal.with(y = BlockStateVariantRotation.R270),
            )
        }
        block.registerCutoutRenderLayer()
        blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        block.enJa(settings.enName, settings.jaName)
        val poemList = PoemList(settings.tier).poem(settings.enPoem, settings.jaPoem)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }
        block.registerBlockTagGeneration { HAIMEVISKA_LOGS }

        block.registerDefaultLootTableGeneration()

    }
}

open class FairyBuildingBlock(val cardGetter: () -> FairyBuildingCard<*, *, *>, settings: Settings) : SimpleHorizontalFacingBlock(settings), BlockEntityProvider {
    companion object {
        private val SHAPE = VoxelShapes.union(
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 0.1),
            createCuboidShape(0.0, 0.0, 0.0, 16.0, 0.1, 16.0),
            createCuboidShape(0.0, 0.0, 0.0, 0.1, 16.0, 16.0),
            createCuboidShape(0.0, 0.0, 15.9, 16.0, 16.0, 16.0),
            createCuboidShape(0.0, 15.9, 0.0, 16.0, 16.0, 16.0),
            createCuboidShape(15.9, 0.0, 0.0, 16.0, 16.0, 16.0),
        )
    }

    // Block Entity

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = cardGetter().blockEntityAccessor.create(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        if (itemStack.hasCustomName()) {
            val blockEntity = cardGetter().blockEntityAccessor.castOrNull(world.getBlockEntity(pos)) ?: return
            blockEntity.customName = itemStack.name
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            cardGetter().blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.dropItems()
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }


    // Move

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if (world.isClient) null else checkType(type, cardGetter().blockEntityType) { world2, pos, state2, blockEntity ->
            blockEntity.tick(world2, pos, state2)
        }
    }


    // Gui

    @Suppress("OVERRIDE_DEPRECATION")
    override fun createScreenHandlerFactory(state: BlockState, world: World, pos: BlockPos) = world.getBlockEntity(pos) as? NamedScreenHandlerFactory

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS
        val blockEntity = cardGetter().blockEntityAccessor.castOrNull(world.getBlockEntity(pos)) ?: return ActionResult.CONSUME
        player.openHandledScreen(object : ExtendedScreenHandlerFactory {
            override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity) = blockEntity.createMenu(syncId, playerInventory, player)
            override fun getDisplayName() = blockEntity.displayName
            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) = Unit
        })
        player.incrementStat(Stats.INTERACT_WITH_BREWINGSTAND)
        return ActionResult.CONSUME
    }


    // Status

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos) = 6

    @Suppress("OVERRIDE_DEPRECATION")
    override fun hasComparatorOutput(state: BlockState) = true

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return cardGetter().blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.getComparatorOutput() ?: 0
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE

}

abstract class FairyBuildingBlockEntity<E : FairyBuildingBlockEntity<E>>(private val card: FairyBuildingCard<*, E, *>, pos: BlockPos, state: BlockState) :
    LockableContainerBlockEntity(card.blockEntityType, pos, state), RenderingProxyBlockEntity, SidedInventory {

    abstract val self: E


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

    private val inventory = MutableList(card.slots.size) { EMPTY_ITEM_STACK }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack = inventory.getOrElse(slot) { EMPTY_ITEM_STACK }

    override fun setStack(slot: Int, stack: ItemStack) {
        if (slot in inventory.indices) {
            inventory[slot] = stack
        }
        if (card.slots[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (card.slots[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        if (card.slots[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.removeStack(inventory, slot)
    }

    override fun isValid(slot: Int, stack: ItemStack) = card.slots[slot].filter(stack)

    override fun getAvailableSlots(side: Direction): IntArray {
        val actualSide = when (side) {
            Direction.UP, Direction.DOWN -> side

            else -> {
                val direction = cachedState.getOrNull(HorizontalFacingBlock.FACING) ?: Direction.NORTH
                Direction.fromHorizontal((direction.horizontal + side.horizontal) % 4)
            }
        }
        return card.availableSlotsTable[actualSide.id]
    }

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = dir in card.slots[slot].insertDirections && isValid(slot, stack)

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction) = dir in card.slots[slot].extractDirections

    override fun clear() {
        world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        inventory.replaceAll { EMPTY_ITEM_STACK }
    }

    fun dropItems() {
        inventory.forEachIndexed { index, itemStack ->
            if (card.slots[index].dropItem) ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack)
        }
        world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }


    // Move

    open fun tick(world: World, pos: BlockPos, state: BlockState) = Unit

    open fun getComparatorOutput() = 0


    // Rendering

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val world = world ?: return
        val blockState = world.getBlockState(pos)
        if (!blockState.isOf(card.block)) return
        val direction = blockState.getOrNull(HorizontalFacingBlock.FACING) ?: return

        renderingProxy.stack {
            renderingProxy.translate(0.5, 0.5, 0.5)
            renderingProxy.rotateY(-((direction.horizontal + 2) * 90) / 180F * Math.PI.toFloat())
            renderingProxy.translate(-0.5, -0.5, -0.5)

            card.slots.forEachIndexed { index, slot ->
                val appearance = slot.appearance ?: return@forEachIndexed
                renderingProxy.stack {
                    renderingProxy.translate(appearance.x / 16.0, appearance.y / 16.0, appearance.z / 16.0) // 移動
                    renderingProxy.rotateY(-appearance.yaw.toFloat() / 180F * 3.14F) // 横回転
                    renderingProxy.rotateX(-appearance.pitch.toFloat() / 180F * 3.14F) // 足元を起点にして縦回転
                    renderingProxy.scale(0.5F, 0.5F, 0.5F) // 縮小
                    renderingProxy.translate(0.0, 2.0 / 16.0, 0.0) // なぜか4ドット分下に埋まるのを補正
                    renderingProxy.renderItemStack(inventory[index])
                }
            }

            renderExtra(renderingProxy, tickDelta, light, overlay)

        }
    }

    open fun renderExtra(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) = Unit


    // Gui

    private val propertyDelegate = object : PropertyDelegate {
        override fun size() = card.properties.size
        override fun get(index: Int) = card.properties.getOrNull(index)?.getter?.invoke(self) ?: 0
        override fun set(index: Int, value: Int) = unit { card.properties.getOrNull(index)?.setter?.invoke(self, value) }
    }

    override fun canPlayerUse(player: PlayerEntity) = Inventory.canPlayerUse(this, player)

    override fun getContainerName(): Text = card.block.name

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): FairyBuildingScreenHandler {
        val arguments = FairyBuildingScreenHandler.Arguments(
            syncId,
            playerInventory,
            this,
            propertyDelegate,
            ScreenHandlerContext.create(world, pos),
        )
        return card.settings.createScreenHandler(arguments)
    }

}

open class FairyBuildingScreenHandler(private val card: FairyBuildingCard<*, *, *>, val arguments: Arguments) : ScreenHandler(card.screenHandlerType, arguments.syncId) {

    class Arguments(
        val syncId: Int,
        val playerInventory: PlayerInventory,
        val inventory: Inventory,
        val propertyDelegate: PropertyDelegate,
        val context: ScreenHandlerContext,
    )

    init {
        checkSize(arguments.inventory, card.slots.size)
        checkDataCount(arguments.propertyDelegate, card.properties.size)

        val y = card.settings.guiHeight - 82
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(arguments.playerInventory, 9 + r * 9 + c, 8 + c * 18, y + r * 18))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(arguments.playerInventory, c, 8 + c * 18, y + 18 * 3 + 4))
        }
        card.slots.forEachIndexed { index, slot ->
            addSlot(object : Slot(arguments.inventory, index, slot.x, slot.y) {
                override fun canInsert(stack: ItemStack) = slot.filter(stack)
            })
        }

        @Suppress("LeakingThis")
        addProperties(arguments.propertyDelegate)
    }

    override fun canUse(player: PlayerEntity) = arguments.inventory.canPlayerUse(player)

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        if (slot < 0 || slot >= slots.size) return EMPTY_ITEM_STACK
        if (!slots[slot].hasStack()) return EMPTY_ITEM_STACK // そこに何も無い場合は何もしない

        val newItemStack = slots[slot].stack
        val originalItemStack = newItemStack.copy()

        if (slot < 9 * 4) { // 上へ
            if (!inventoryAccessor.insertItem(newItemStack, 9 * 4 until slots.size)) return EMPTY_ITEM_STACK
        } else { // 下へ
            if (!inventoryAccessor.insertItem(newItemStack, 9 * 4 - 1 downTo 0)) return EMPTY_ITEM_STACK
            slots[slot].onQuickTransfer(newItemStack, originalItemStack)
        }

        // 終了処理
        if (newItemStack.isEmpty) {
            slots[slot].stack = EMPTY_ITEM_STACK
        } else {
            slots[slot].markDirty()
        }

        return originalItemStack
    }

    inner class Property(private val property: FairyBuildingSettings.PropertySettings<*>) {
        operator fun getValue(thisRef: Any?, property: Any?): Int {
            val propertyIndex = card.propertyIndexTable[this.property] ?: throw NullPointerException("No such property")
            return this.property.decoder(arguments.propertyDelegate.get(propertyIndex).toShort())
        }

        operator fun setValue(thisRef: Any?, property: Any?, value: Int) {
            val propertyIndex = card.propertyIndexTable[this.property] ?: throw NullPointerException("No such property")
            arguments.propertyDelegate.set(propertyIndex, this.property.encoder(value).toInt())
        }
    }

}
