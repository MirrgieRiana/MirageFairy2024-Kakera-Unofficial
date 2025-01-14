package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.lib.HorizontalFacingMachineBlock
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.EnJa
import miragefairy2024.util.checkType
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.getOrNull
import miragefairy2024.util.normal
import miragefairy2024.util.quickMove
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
import miragefairy2024.util.withHorizontalRotation
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
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

@Suppress("LeakingThis") // ブートストラップ問題のため解決不可能なので妥協する
abstract class FairyBuildingCard<B : FairyBuildingBlock, E : FairyBuildingBlockEntity<E>, H : FairyBuildingScreenHandler> : MachineCard<B, E, H>() {

    // Specification

    abstract fun getPath(): String
    override fun createIdentifier() = MirageFairy2024.identifier(getPath())

    abstract val tier: Int
    abstract val name: EnJa
    abstract val poem: EnJa


    // Block

    override fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().nonOpaque().strength(2.0F).instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.RAW_IRON_PINK)


    // ScreenHandler

    abstract fun createScreenHandler(arguments: FairyBuildingScreenHandler.Arguments): H

    val screenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
        val arguments = FairyBuildingScreenHandler.Arguments(
            syncId,
            playerInventory,
            SimpleInventory(slotConfigurations.size),
            ArrayPropertyDelegate(propertyConfigurations.size),
            ScreenHandlerContext.EMPTY,
        )
        createScreenHandler(arguments)
    }


    // Gui

    abstract val guiWidth: Int
    abstract val guiHeight: Int

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"


    // Slot

    class FairyBuildingSlotConfiguration(
        val x: Int,
        val y: Int,
        val dropItem: Boolean = true,
        val insertDirections: Set<Direction> = setOf(),
        val extractDirections: Set<Direction> = setOf(),
        val animation: SlotAnimationConfiguration? = null,
        val toolTipGetter: (() -> List<Text>)? = null,
        val filter: (ItemStack) -> Boolean = { true },
    )

    class SlotAnimationConfiguration(val isFairy: Boolean, val positions: List<Position>)

    /**
     * @param x 1/16 scale
     * @param y 1/16 scale
     * @param z 1/16 scale
     * @param pitch degree
     * @param yaw degree
     */
    class Position(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float, val duration: Int)

    open fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> = listOf()
    val slotConfigurations = createSlotConfigurations()

    val availableSlotsTable = arrayOf(
        slotConfigurations.withIndex().filter { Direction.DOWN in it.value.insertDirections || Direction.DOWN in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.UP in it.value.insertDirections || Direction.UP in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.NORTH in it.value.insertDirections || Direction.NORTH in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.SOUTH in it.value.insertDirections || Direction.SOUTH in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.WEST in it.value.insertDirections || Direction.WEST in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.EAST in it.value.insertDirections || Direction.EAST in it.value.extractDirections }.map { it.index }.toIntArray(),
    )


    // Property

    class FairyBuildingPropertyConfiguration<in E : FairyBuildingBlockEntity<*>>(
        val getter: E.() -> Int,
        val setter: E.(Int) -> Unit,
        val encoder: (Int) -> Short = { it.toShort() },
        val decoder: (Short) -> Int = { it.toInt() },
    )

    open fun createPropertyConfigurations(): List<FairyBuildingPropertyConfiguration<E>> = listOf()
    val propertyConfigurations = createPropertyConfigurations()

    val propertyIndexTable = propertyConfigurations.withIndex().associate { (index, it) -> it to index }


    context(ModContext)
    open fun init() {

        block.register(Registries.BLOCK, identifier)
        blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, identifier)
        item.register(Registries.ITEM, identifier)
        screenHandlerType.register(Registries.SCREEN_HANDLER, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        block.registerVariantsBlockStateGeneration { normal("block/" * block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        block.registerCutoutRenderLayer()
        blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        block.enJa(name)
        val poemList = PoemList(tier).poem(poem)
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }
        block.registerBlockTagGeneration { HAIMEVISKA_LOGS }

        block.registerDefaultLootTableGeneration()

    }
}

open class FairyBuildingBlock(private val card: FairyBuildingCard<*, *, *>) : HorizontalFacingMachineBlock(card), BlockEntityProvider {
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

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = card.blockEntityAccessor.create(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        if (itemStack.hasCustomName()) {
            val blockEntity = card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos)) ?: return
            blockEntity.customName = itemStack.name
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.dropItems()
            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }


    // Move

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if (world.isClient) checkType(type, card.blockEntityType) { world2, pos, state2, blockEntity ->
            blockEntity.clientTick(world2, pos, state2)
        } else checkType(type, card.blockEntityType) { world2, pos, state2, blockEntity ->
            blockEntity.serverTick(world2, pos, state2)
        }
    }


    // Gui

    @Suppress("OVERRIDE_DEPRECATION")
    override fun createScreenHandlerFactory(state: BlockState, world: World, pos: BlockPos) = world.getBlockEntity(pos) as? NamedScreenHandlerFactory

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS
        val blockEntity = card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos)) ?: return ActionResult.CONSUME
        player.openHandledScreen(object : ExtendedScreenHandlerFactory {
            override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity) = blockEntity.createMenu(syncId, playerInventory, player)
            override fun getDisplayName() = blockEntity.displayName
            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) = Unit
        })
        player.incrementStat(Stats.USED.getOrCreateStat(this.asItem()))
        return ActionResult.CONSUME
    }


    // Status

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos) = 6

    @Suppress("OVERRIDE_DEPRECATION")
    override fun hasComparatorOutput(state: BlockState) = true

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.getComparatorOutput() ?: 0
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE

}

abstract class FairyBuildingBlockEntity<E : FairyBuildingBlockEntity<E>>(private val card: FairyBuildingCard<*, E, *>, pos: BlockPos, state: BlockState) : MachineBlockEntity<E>(card, card.blockEntityType, pos, state), RenderingProxyBlockEntity, SidedInventory {

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

    private val inventory = MutableList(card.slotConfigurations.size) { EMPTY_ITEM_STACK }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack = inventory.getOrElse(slot) { EMPTY_ITEM_STACK }

    override fun setStack(slot: Int, stack: ItemStack) {
        if (slot in inventory.indices) {
            inventory[slot] = stack
        }
        if (card.slotConfigurations[slot].animation != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (card.slotConfigurations[slot].animation != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        if (card.slotConfigurations[slot].animation != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.removeStack(inventory, slot)
    }

    override fun isValid(slot: Int, stack: ItemStack) = card.slotConfigurations[slot].filter(stack)

    private fun getActualSide(side: Direction): Direction {
        return when (side) {
            Direction.UP, Direction.DOWN -> side

            else -> {
                val direction = cachedState.getOrNull(HorizontalFacingBlock.FACING) ?: Direction.NORTH
                Direction.fromHorizontal((direction.horizontal + side.horizontal) % 4)
            }
        }
    }

    override fun getAvailableSlots(side: Direction): IntArray {
        return card.availableSlotsTable[getActualSide(side).id]
    }

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = (dir == null || (getActualSide(dir) in card.slotConfigurations[slot].insertDirections)) && isValid(slot, stack)

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction) = getActualSide(dir) in card.slotConfigurations[slot].extractDirections

    override fun clear() {
        world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        inventory.replaceAll { EMPTY_ITEM_STACK }
    }

    fun dropItems() {
        inventory.forEachIndexed { index, itemStack ->
            if (card.slotConfigurations[index].dropItem) ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack)
        }
        world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }


    // Move

    open fun serverTick(world: World, pos: BlockPos, state: BlockState) = Unit

    open fun getComparatorOutput() = 0


    // Rendering

    protected open val doMovePosition get() = false

    private val fairyAnimators = Array(card.slotConfigurations.size) { index ->
        val animation = card.slotConfigurations[index].animation
        if (animation != null) FairyAnimator(animation) else null
    }

    private inner class FairyAnimator(val animation: FairyBuildingCard.SlotAnimationConfiguration) {
        init {
            check(animation.positions.isNotEmpty())
        }

        private var index = 0
        private var position = animation.positions[index]
        private var countdown = position.duration

        var ticks = (Math.random() * 1000).toInt()
        var xSpeed = 0.0
        var ySpeed = 0.0
        var zSpeed = 0.0
        var yawSpeed = 0.0F
        var pitchSpeed = 0.0F
        var x = position.x
        var y = position.y
        var z = position.z
        var yaw = position.yaw
        var pitch = position.pitch

        fun tick(doMovePosition: Boolean) {
            val world = world ?: return

            // 定位置の切り替え
            if (doMovePosition) {
                countdown--
                if (countdown <= 0) {

                    index++
                    if (index >= animation.positions.size) index = 0

                    position = animation.positions[index]
                    countdown = (animation.positions[index].duration * (1.0 + world.random.nextDouble() * 0.1)).toInt()

                }
            }

            // 妖精の移動
            ticks++
            xSpeed = (position.x - x) * 0.1
            ySpeed = (position.y - y) * 0.1
            zSpeed = (position.z - z) * 0.1
            yawSpeed = (position.yaw - yaw) * 0.1F
            pitchSpeed = (position.pitch - pitch) * 0.1F
            x += xSpeed
            y += ySpeed
            z += zSpeed
            yaw += yawSpeed
            pitch += pitchSpeed

        }
    }

    open fun clientTick(world: World, pos: BlockPos, state: BlockState) {
        cachedState
        fairyAnimators.forEach {
            it?.tick(doMovePosition)
        }
    }

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val world = world ?: return
        val blockState = world.getBlockState(pos)
        if (!blockState.isOf(card.block)) return
        val direction = blockState.getOrNull(HorizontalFacingBlock.FACING) ?: return

        renderingProxy.stack {
            renderingProxy.translate(0.5, 0.5, 0.5)
            renderingProxy.rotateY(-((direction.horizontal + 2) * 90) / 180F * Math.PI.toFloat())
            renderingProxy.translate(-0.5, -0.5, -0.5)

            card.slotConfigurations.forEachIndexed { index, _ ->
                val fairyAnimator = fairyAnimators[index] ?: return@forEachIndexed

                val x = fairyAnimator.x + fairyAnimator.xSpeed * tickDelta.toDouble()
                val y = fairyAnimator.y + fairyAnimator.ySpeed * tickDelta.toDouble()
                val z = fairyAnimator.z + fairyAnimator.zSpeed * tickDelta.toDouble()
                val yaw = fairyAnimator.yaw + fairyAnimator.yawSpeed * tickDelta
                val pitch = fairyAnimator.pitch + fairyAnimator.pitchSpeed * tickDelta
                val yawOffset = if (fairyAnimator.animation.isFairy) MathHelper.sin((fairyAnimator.ticks.toFloat() + tickDelta) * 0.03F) * 3F else 0F
                val pitchOffset = if (fairyAnimator.animation.isFairy) MathHelper.sin((fairyAnimator.ticks.toFloat() + tickDelta) * 0.08F) * 5F else 0F

                renderingProxy.stack {
                    renderingProxy.translate(x / 16.0, y / 16.0, z / 16.0) // 移動
                    renderingProxy.rotateY(-yaw / 180F * MathHelper.PI) // 横回転
                    renderingProxy.rotateX(-pitch / 180F * MathHelper.PI) // 足元を起点にして縦回転
                    renderingProxy.scale(0.5F, 0.5F, 0.5F) // 縮小

                    if (fairyAnimator.animation.isFairy) {
                        renderingProxy.translate(0.0, 0.25, 0.0)
                        renderingProxy.rotateY(-yawOffset / 180F * MathHelper.PI) // 横回転
                        renderingProxy.rotateZ(-pitchOffset / 180F * MathHelper.PI) // 上下回転
                        renderingProxy.translate(0.0, -0.25, 0.0)
                    }

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
        override fun size() = card.propertyConfigurations.size
        override fun get(index: Int) = card.propertyConfigurations.getOrNull(index)?.let { it.encoder(it.getter.invoke(getThis())).toInt() } ?: 0
        override fun set(index: Int, value: Int) = unit { card.propertyConfigurations.getOrNull(index)?.let { it.setter.invoke(getThis(), it.decoder(value.toShort())) } }
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
        return card.createScreenHandler(arguments)
    }

}

open class FairyBuildingScreenHandler(private val card: FairyBuildingCard<*, *, *>, val arguments: Arguments) : MachineScreenHandler(card, card.screenHandlerType, arguments.syncId) {

    class Arguments(
        val syncId: Int,
        val playerInventory: PlayerInventory,
        val inventory: Inventory,
        val propertyDelegate: PropertyDelegate,
        val context: ScreenHandlerContext,
    )

    init {
        checkSize(arguments.inventory, card.slotConfigurations.size)
        checkDataCount(arguments.propertyDelegate, card.propertyConfigurations.size)

        val y = card.guiHeight - 82
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(arguments.playerInventory, 9 + r * 9 + c, 8 + c * 18, y + r * 18))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(arguments.playerInventory, c, 8 + c * 18, y + 18 * 3 + 4))
        }
        card.slotConfigurations.forEachIndexed { index, slot ->
            addSlot(object : Slot(arguments.inventory, index, slot.x, slot.y) {
                override fun canInsert(stack: ItemStack) = slot.filter(stack)
            })
        }

        @Suppress("LeakingThis")
        addProperties(arguments.propertyDelegate)
    }

    override fun canUse(player: PlayerEntity) = arguments.inventory.canPlayerUse(player)

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        val playerIndices = 9 * 4 - 1 downTo 0
        val utilityIndices = 9 * 4 until slots.size
        val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
        return quickMove(slot, destinationIndices)
    }

    inner class Property(private val property: FairyBuildingCard.FairyBuildingPropertyConfiguration<*>) {
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
