package miragefairy2024.mod.fairybuilding

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.lib.SimpleMachineScreenHandler
import miragefairy2024.lib.createScreenHandlerType
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
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
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

abstract class FairyBuildingConfiguration<C : FairyBuildingCard<C, S, B, E, H>, S : FairyBuildingConfiguration<C, S, B, E, H>, B : FairyBuildingBlock<C>, E : FairyBuildingBlockEntity<C, E>, H : FairyBuildingScreenHandler<C>> {
    companion object {
        inline fun <C : FairyBuildingCard<C, *, *, E, *>, reified E : FairyBuildingBlockEntity<C, E>> BlockEntityAccessor(crossinline creator: (card: C, blockPos: BlockPos, blockState: BlockState) -> E) = object : BlockEntityAccessor<C, E> {
            override fun create(card: C, blockPos: BlockPos, blockState: BlockState) = creator(card, blockPos, blockState)
            override fun castOrThrow(blockEntity: BlockEntity?) = blockEntity as E
            override fun castOrNull(blockEntity: BlockEntity?) = blockEntity as? E
        }
    }


    abstract val path: String
    abstract val tier: Int
    abstract val name: EnJa
    abstract val poem: EnJa


    open fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().nonOpaque().strength(2.0F).instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).mapColor(MapColor.RAW_IRON_PINK)

    abstract fun createBlock(cardGetter: () -> C, settings: FabricBlockSettings): B


    abstract fun createBlockEntityAccessor(): BlockEntityAccessor<C, E>

    interface BlockEntityAccessor<C : FairyBuildingCard<C, *, *, E, *>, E : FairyBuildingBlockEntity<C, E>> {
        fun create(card: C, blockPos: BlockPos, blockState: BlockState): E
        fun castOrThrow(blockEntity: BlockEntity?): E
        fun castOrNull(blockEntity: BlockEntity?): E?
    }


    abstract fun createScreenHandler(card: C, arguments: SimpleMachineScreenHandler.Arguments<SimpleMachineScreenHandler.Configuration>): H


    abstract val guiWidth: Int
    abstract val guiHeight: Int


    open fun createSlotConfigurations(): List<SlotConfiguration> = listOf()

    class SlotConfiguration(
        val x: Int,
        val y: Int,
        val dropItem: Boolean = true,
        val insertDirections: Set<Direction> = setOf(),
        val extractDirections: Set<Direction> = setOf(),
        val appearance: Appearance? = null,
        val toolTipGetter: (() -> List<Text>)? = null,
        val filter: (ItemStack) -> Boolean = { true },
    )

    class Appearance(val isFairy: Boolean, val positions: List<Position>)

    /**
     * @param x 1/16 scale
     * @param y 1/16 scale
     * @param z 1/16 scale
     * @param pitch degree
     * @param yaw degree
     */
    class Position(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float, val duration: Int)


    open fun createPropertyConfigurations(): List<FairyBuildingPropertyConfiguration<E>> = listOf()

    interface FairyBuildingPropertyConfiguration<in E : FairyBuildingBlockEntity<*, *>> : SimpleMachineScreenHandler.PropertyConfiguration {
        fun createProperty(blockEntity: E): SimpleMachineScreenHandler.Property
    }


    context(ModContext)
    open fun init(card: C) {

        card.block.register(Registries.BLOCK, card.identifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)
        card.screenHandlerType.register(Registries.SCREEN_HANDLER, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        card.block.registerCutoutRenderLayer()
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        card.block.enJa(name)
        val poemList = PoemList(tier).poem(poem)
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        card.block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }
        card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS }

        card.block.registerDefaultLootTableGeneration()

    }
}

abstract class FairyBuildingCard<C : FairyBuildingCard<C, S, B, E, H>, S : FairyBuildingConfiguration<C, S, B, E, H>, B : FairyBuildingBlock<C>, E : FairyBuildingBlockEntity<C, E>, H : FairyBuildingScreenHandler<C>>(val configuration: S) {
    abstract val self: C

    val identifier = MirageFairy2024.identifier(configuration.path)

    val block = configuration.createBlock({ self }, configuration.createBlockSettings())

    val blockEntityAccessor = configuration.createBlockEntityAccessor()
    val blockEntityType = BlockEntityType({ pos, state -> blockEntityAccessor.create(self, pos, state) }, setOf(block), null)
    fun createBlockEntity(pos: BlockPos, state: BlockState) = blockEntityAccessor.create(self, pos, state)

    val item = BlockItem(block, Item.Settings())

    val slotConfigurations = configuration.createSlotConfigurations()
    val availableSlotsTable = arrayOf(
        slotConfigurations.withIndex().filter { Direction.DOWN in it.value.insertDirections || Direction.DOWN in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.UP in it.value.insertDirections || Direction.UP in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.NORTH in it.value.insertDirections || Direction.NORTH in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.SOUTH in it.value.insertDirections || Direction.SOUTH in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.WEST in it.value.insertDirections || Direction.WEST in it.value.extractDirections }.map { it.index }.toIntArray(),
        slotConfigurations.withIndex().filter { Direction.EAST in it.value.insertDirections || Direction.EAST in it.value.extractDirections }.map { it.index }.toIntArray(),
    )

    val propertyConfigurations = configuration.createPropertyConfigurations()

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"

    val screenHandlerConfiguration = object : SimpleMachineScreenHandler.Configuration {
        override val type = run { this@FairyBuildingCard }.screenHandlerType
        override val width = configuration.guiWidth
        override val height = configuration.guiHeight
        override val machineSlotConfigurations = slotConfigurations.map {
            SimpleMachineScreenHandler.SlotConfiguration(it.x, it.y) / TODO
        }
        override val propertyConfigurations = this@FairyBuildingCard.propertyConfigurations
    }
    val screenHandlerType: ExtendedScreenHandlerType<H> = screenHandlerConfiguration.createScreenHandlerType { arguments, _ -> createScreenHandler(arguments) }
    fun createProperties(blockEntity: E) = propertyConfigurations.map { it.createProperty(blockEntity) }
    fun createScreenHandler(arguments: SimpleMachineScreenHandler.Arguments<SimpleMachineScreenHandler.Configuration>) = configuration.createScreenHandler(self, arguments)

    context(ModContext)
    fun init() = configuration.init(self)
}

open class FairyBuildingBlock<C : FairyBuildingCard<C, *, *, *, *>>(val cardGetter: () -> C, settings: Settings) :
    SimpleHorizontalFacingBlock(settings),
    BlockEntityProvider {
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

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = cardGetter().createBlockEntity(pos, state)

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
        return if (world.isClient) checkType(type, cardGetter().blockEntityType) { world2, pos, state2, blockEntity ->
            blockEntity.clientTick(world2, pos, state2)
        } else checkType(type, cardGetter().blockEntityType) { world2, pos, state2, blockEntity ->
            blockEntity.serverTick(world2, pos, state2)
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
        return cardGetter().blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.getComparatorOutput() ?: 0
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPE

}

abstract class FairyBuildingBlockEntity<C : FairyBuildingCard<C, *, *, E, *>, E : FairyBuildingBlockEntity<C, E>>(val card: C, pos: BlockPos, state: BlockState) :
    LockableContainerBlockEntity(card.blockEntityType, pos, state),
    RenderingProxyBlockEntity,
    SidedInventory {

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

    private val inventory = MutableList(card.slotConfigurations.size) { EMPTY_ITEM_STACK }

    override fun size() = inventory.size

    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack = inventory.getOrElse(slot) { EMPTY_ITEM_STACK }

    override fun setStack(slot: Int, stack: ItemStack) {
        if (slot in inventory.indices) {
            inventory[slot] = stack
        }
        if (card.slotConfigurations[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (card.slotConfigurations[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        markDirty()
        return Inventories.splitStack(inventory, slot, amount)
    }

    override fun removeStack(slot: Int): ItemStack {
        if (card.slotConfigurations[slot].appearance != null) world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
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
        val appearance = card.slotConfigurations[index].appearance
        if (appearance != null) FairyAnimator(appearance) else null
    }

    private inner class FairyAnimator(val appearance: FairyBuildingConfiguration.Appearance) {
        init {
            check(appearance.positions.isNotEmpty())
        }

        private var index = 0
        private var position = appearance.positions[index]
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
                    if (index >= appearance.positions.size) index = 0

                    position = appearance.positions[index]
                    countdown = (appearance.positions[index].duration * (1.0 + world.random.nextDouble() * 0.1)).toInt()

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
                val yawOffset = if (fairyAnimator.appearance.isFairy) MathHelper.sin((fairyAnimator.ticks.toFloat() + tickDelta) * 0.03F) * 3F else 0F
                val pitchOffset = if (fairyAnimator.appearance.isFairy) MathHelper.sin((fairyAnimator.ticks.toFloat() + tickDelta) * 0.08F) * 5F else 0F

                renderingProxy.stack {
                    renderingProxy.translate(x / 16.0, y / 16.0, z / 16.0) // 移動
                    renderingProxy.rotateY(-yaw / 180F * MathHelper.PI) // 横回転
                    renderingProxy.rotateX(-pitch / 180F * MathHelper.PI) // 足元を起点にして縦回転
                    renderingProxy.scale(0.5F, 0.5F, 0.5F) // 縮小

                    if (fairyAnimator.appearance.isFairy) {
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

    override fun canPlayerUse(player: PlayerEntity) = Inventory.canPlayerUse(this, player)

    override fun getContainerName(): Text = card.block.name

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        val arguments = SimpleMachineScreenHandler.Arguments(
            card.screenHandlerConfiguration,
            syncId,
            playerInventory,
            this,
            card.createProperties(self),
            ScreenHandlerContext.create(world, pos),
        )
        return card.createScreenHandler(arguments)
    }

}

open class FairyBuildingScreenHandler<C : FairyBuildingCard<C, *, *, *, *>>(val card: C, arguments: Arguments<Configuration>) :
    SimpleMachineScreenHandler<SimpleMachineScreenHandler.Configuration>(arguments) {

    override fun canUse(player: PlayerEntity) = canUse(arguments.context, player, card.block)

}
