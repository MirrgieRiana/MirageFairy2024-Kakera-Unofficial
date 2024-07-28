package miragefairy2024.mod.fairyhouse

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.fairyhouse.AbstractFairyHouseBlockEntity.PropertySettings
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaLeavesBlock
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.BlockStateVariantRotation
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.checkType
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.getOrNull
import miragefairy2024.util.insertItem
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
import net.minecraft.world.BlockView
import net.minecraft.world.World

context(ModContext)
fun initFairyHouseModule() {
    FairyHouseCard.init()
}


interface BlockEntityAccessor<E : BlockEntity> {
    fun create(blockPos: BlockPos, blockState: BlockState): E
    fun castOrThrow(blockEntity: BlockEntity?): E
    fun castOrNull(blockEntity: BlockEntity?): E?
}

inline fun <reified E : BlockEntity> BlockEntityAccessor(crossinline creator: (blockPos: BlockPos, blockState: BlockState) -> E) = object : BlockEntityAccessor<E> {
    override fun create(blockPos: BlockPos, blockState: BlockState) = creator(blockPos, blockState)
    override fun castOrThrow(blockEntity: BlockEntity?) = blockEntity as E
    override fun castOrNull(blockEntity: BlockEntity?) = blockEntity as? E
}

open class AbstractFairyHouseCard<B : AbstractFairyHouseBlock<E>, E : AbstractFairyHouseBlockEntity<E>>(
    path: String,
    private val tier: Int,
    private val enName: String,
    private val jaName: String,
    private val enPoem: String,
    private val jaPoem: String,
    blockCreator: (FabricBlockSettings) -> B,
    val blockEntityAccessor: BlockEntityAccessor<E>,
    val screenHandlerCreator: (AbstractFairyHouseScreenHandler.Arguments) -> AbstractFairyHouseScreenHandler<E>,
    val guiWidth: Int,
    val guiHeight: Int,
    oldBlockEntitySettings: AbstractFairyHouseBlockEntity.Settings<E>,
) {
    val foliaProperty = PropertySettings<E>({ folia }, { folia = it })
    val blockEntitySettings = AbstractFairyHouseBlockEntity.Settings(
        oldBlockEntitySettings.slots,
        listOf(foliaProperty) + oldBlockEntitySettings.properties,
    )
    val propertyIndexTable = blockEntitySettings.properties.withIndex().associate { (index, it) -> it to index }
    val identifier = Identifier(MirageFairy2024.modId, path)
    val block = blockCreator(FabricBlockSettings.create().nonOpaque().strength(2.0F).instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.RAW_IRON_PINK))
    val blockEntityType = BlockEntityType(blockEntityAccessor::create, setOf(block), null)
    val item = BlockItem(block, Item.Settings())
    val screenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
        val arguments = AbstractFairyHouseScreenHandler.Arguments(
            syncId,
            playerInventory,
            SimpleInventory(blockEntitySettings.slots.size),
            ArrayPropertyDelegate(blockEntitySettings.properties.size),
            ScreenHandlerContext.EMPTY,
        )
        screenHandlerCreator(arguments)
    }

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

        block.enJa(enName, jaName)
        val poemList = PoemList(tier).poem(enPoem, jaPoem)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }
        block.registerBlockTagGeneration { HAIMEVISKA_LOGS }

        block.registerDefaultLootTableGeneration()

    }
}

open class AbstractFairyHouseBlock<E : AbstractFairyHouseBlockEntity<E>>(
    val cardGetter: () -> AbstractFairyHouseCard<*, E>,
    settings: Settings,
) : SimpleHorizontalFacingBlock(settings), BlockEntityProvider {

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
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos) = 4

    @Suppress("OVERRIDE_DEPRECATION")
    override fun hasComparatorOutput(state: BlockState) = true

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return cardGetter().blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.getComparatorOutput() ?: 0
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

}

abstract class AbstractFairyHouseBlockEntity<E : AbstractFairyHouseBlockEntity<E>>(
    val card: AbstractFairyHouseCard<*, E>,
    pos: BlockPos,
    state: BlockState,
) : LockableContainerBlockEntity(card.blockEntityType, pos, state), RenderingProxyBlockEntity, SidedInventory {

    abstract val self: E


    // Settings

    class Settings<E : AbstractFairyHouseBlockEntity<*>>(val slots: List<SlotSettings> = listOf(), val properties: List<PropertySettings<E>> = listOf()) {
        val availableSlotsTable = arrayOf(
            slots.withIndex().filter { Direction.DOWN in it.value.insertDirections || Direction.DOWN in it.value.extractDirections }.map { it.index }.toIntArray(),
            slots.withIndex().filter { Direction.UP in it.value.insertDirections || Direction.UP in it.value.extractDirections }.map { it.index }.toIntArray(),
            slots.withIndex().filter { Direction.NORTH in it.value.insertDirections || Direction.NORTH in it.value.extractDirections }.map { it.index }.toIntArray(),
            slots.withIndex().filter { Direction.SOUTH in it.value.insertDirections || Direction.SOUTH in it.value.extractDirections }.map { it.index }.toIntArray(),
            slots.withIndex().filter { Direction.WEST in it.value.insertDirections || Direction.WEST in it.value.extractDirections }.map { it.index }.toIntArray(),
            slots.withIndex().filter { Direction.EAST in it.value.insertDirections || Direction.EAST in it.value.extractDirections }.map { it.index }.toIntArray(),
        )
    }

    class SlotSettings(
        val x: Int,
        val y: Int,
        val dropItem: Boolean = true,
        val insertDirections: Set<Direction> = setOf(),
        val extractDirections: Set<Direction> = setOf(),
        val appearance: Appearance? = null,
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

    class PropertySettings<E>(val getter: E.() -> Int, val setter: E.(Int) -> Unit)


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

    private val inventory = MutableList(card.blockEntitySettings.slots.size) { EMPTY_ITEM_STACK }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack = inventory.getOrElse(slot) { EMPTY_ITEM_STACK }

    override fun setStack(slot: Int, stack: ItemStack) {
        if (slot in inventory.indices) {
            inventory[slot] = stack
        }
        if (card.blockEntitySettings.slots[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (card.blockEntitySettings.slots[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        if (card.blockEntitySettings.slots[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.removeStack(inventory, slot)
    }

    override fun isValid(slot: Int, stack: ItemStack) = card.blockEntitySettings.slots[slot].filter(stack)

    override fun getAvailableSlots(side: Direction): IntArray {
        val actualSide = when (side) {
            Direction.UP, Direction.DOWN -> side

            else -> {
                val direction = cachedState.getOrNull(HorizontalFacingBlock.FACING) ?: Direction.NORTH
                Direction.fromHorizontal((direction.horizontal + side.horizontal) % 4)
            }
        }
        return card.blockEntitySettings.availableSlotsTable[actualSide.id]
    }

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = dir in card.blockEntitySettings.slots[slot].insertDirections && isValid(slot, stack)

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction) = dir in card.blockEntitySettings.slots[slot].extractDirections

    override fun clear() {
        world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        inventory.replaceAll { EMPTY_ITEM_STACK }
    }

    fun dropItems() {
        inventory.forEachIndexed { index, itemStack ->
            if (card.blockEntitySettings.slots[index].dropItem) ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack)
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

            card.blockEntitySettings.slots.forEachIndexed { index, slot ->
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
        override fun size() = card.blockEntitySettings.properties.size
        override fun get(index: Int) = card.blockEntitySettings.properties.getOrNull(index)?.getter?.invoke(self) ?: 0
        override fun set(index: Int, value: Int) = unit { card.blockEntitySettings.properties.getOrNull(index)?.setter?.invoke(self, value) }
    }

    override fun canPlayerUse(player: PlayerEntity) = Inventory.canPlayerUse(this, player)

    override fun getContainerName(): Text = card.block.name

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): AbstractFairyHouseScreenHandler<E> {
        val arguments = AbstractFairyHouseScreenHandler.Arguments(
            syncId,
            playerInventory,
            this,
            propertyDelegate,
            ScreenHandlerContext.create(world, pos),
        )
        return card.screenHandlerCreator(arguments)
    }


    // Fairy House

    var folia = 0

    fun collectFolia() {
        val world = world ?: return

        // 最大200ブロックのハイメヴィスカの原木を探す
        val logs = blockVisitor(listOf(pos), maxCount = 200, neighborType = NeighborType.VERTICES) { _, toBlockPos ->
            world.getBlockState(toBlockPos).isIn(HAIMEVISKA_LOGS)
        }.map { it.second }.toList()

        // 最大距離6までの葉をすべて探す
        var changed = false
        run finished@{
            blockVisitor(logs, visitOrigins = false, maxDistance = 6) { _, toBlockPos ->
                world.getBlockState(toBlockPos).isOf(HaimeviskaBlockCard.LEAVES.block)
            }.forEach { (_, blockPos) ->
                val blockState = world.getBlockState(blockPos)
                if (blockState[HaimeviskaLeavesBlock.CHARGED]) {
                    folia += 100
                    changed = true
                    world.setBlockState(blockPos, blockState.with(HaimeviskaLeavesBlock.CHARGED, false), Block.NOTIFY_LISTENERS)
                    if (folia > 10000) return@finished
                }
            }
        }

        if (changed) markDirty()

    }

}

open class AbstractFairyHouseScreenHandler<E : AbstractFairyHouseBlockEntity<E>>(
    val card: AbstractFairyHouseCard<*, E>,
    val arguments: Arguments,
) : ScreenHandler(card.screenHandlerType, arguments.syncId) {

    class Arguments(
        val syncId: Int,
        val playerInventory: PlayerInventory,
        val inventory: Inventory,
        val propertyDelegate: PropertyDelegate,
        val context: ScreenHandlerContext,
    )

    init {
        checkSize(arguments.inventory, card.blockEntitySettings.slots.size)
        checkDataCount(arguments.propertyDelegate, card.blockEntitySettings.properties.size)

        val y = card.guiHeight - 82
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(arguments.playerInventory, 9 + r * 9 + c, 8 + c * 18, y + r * 18))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(arguments.playerInventory, c, 8 + c * 18, y + 18 * 3 + 4))
        }
        card.blockEntitySettings.slots.forEachIndexed { index, slot ->
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
            if (!insertItem(newItemStack, 9 * 4 until slots.size)) return EMPTY_ITEM_STACK
        } else { // 下へ
            if (!insertItem(newItemStack, 9 * 4 - 1 downTo 0)) return EMPTY_ITEM_STACK
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

    var folia: Int
        get() = arguments.propertyDelegate.get(card.propertyIndexTable[card.foliaProperty]!!)
        set(value) {
            arguments.propertyDelegate.set(card.propertyIndexTable[card.foliaProperty]!!, value)
        }

}
