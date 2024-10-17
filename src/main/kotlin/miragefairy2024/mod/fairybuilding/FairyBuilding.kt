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
import net.minecraft.util.math.random.Random
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


    abstract fun createScreenHandler(card: C, arguments: SimpleMachineScreenHandler.Arguments): H


    abstract val guiWidth: Int
    abstract val guiHeight: Int


    open fun createSlotConfigurations(): List<FairyBuildingSlotConfiguration> = listOf()

    class FairyBuildingSlotConfiguration(
        override val x: Int,
        override val y: Int,
        override val shouldDropItem: Boolean = true,
        val insertDirections: Set<Direction> = setOf(),
        val extractDirections: Set<Direction> = setOf(),
        val animation: SlotAnimationConfiguration? = null,
        val toolTipGetter: (() -> List<Text>)? = null,
        private val filter: (ItemStack) -> Boolean = { true },
    ) : SimpleMachineScreenHandler.SlotConfiguration, SimpleMachineBlockEntity.SlotConfiguration {
        override fun filter(itemStack: ItemStack) = filter.invoke(itemStack)
        override fun canInsertTo(direction: Direction) = direction in insertDirections
        override fun canExtractFrom(direction: Direction) = direction in extractDirections
    }

    class SlotAnimationConfiguration(val motion: SimpleMachineBlockEntity.Motion, val positions: List<SimpleMachineBlockEntity.Position>)


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

    val propertyConfigurations = configuration.createPropertyConfigurations()

    val backgroundTexture = "textures/gui/container/" * identifier * ".png"

    val screenHandlerConfiguration = object : SimpleMachineScreenHandler.Configuration {
        override val type = run { this@FairyBuildingCard }.screenHandlerType
        override val width = configuration.guiWidth
        override val height = configuration.guiHeight
        override val machineSlotConfigurations = this@FairyBuildingCard.slotConfigurations
        override val propertyConfigurations = this@FairyBuildingCard.propertyConfigurations
    }
    val screenHandlerType: ExtendedScreenHandlerType<H> = screenHandlerConfiguration.createScreenHandlerType { arguments, _ -> createScreenHandler(arguments) }
    fun createProperties(blockEntity: E) = propertyConfigurations.map { it.createProperty(blockEntity) }
    fun createScreenHandler(arguments: SimpleMachineScreenHandler.Arguments) = configuration.createScreenHandler(self, arguments)

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
        return if (world.isClient) {
            checkType(type, cardGetter().blockEntityType) { world2, pos, state2, blockEntity ->
                blockEntity.clientTick(world2, pos, state2)
            }
        } else {
            checkType(type, cardGetter().blockEntityType) { world2, pos, state2, blockEntity ->
                blockEntity.serverTick(world2, pos, state2)
            }
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

abstract class SimpleMachineBlockEntity(val arguments: Arguments) : LockableContainerBlockEntity(arguments.configuration.type, arguments.pos, arguments.state), SidedInventory, RenderingProxyBlockEntity {

    class Arguments(
        val configuration: Configuration,
        val pos: BlockPos,
        val state: BlockState,
    )

    interface Configuration {
        val type: BlockEntityType<*>
        val slotConfigurations: List<SlotConfiguration>
        val screenHandlerConfiguration: SimpleMachineScreenHandler.Configuration
    }

    interface SlotConfiguration {
        fun filter(itemStack: ItemStack): Boolean
        fun canInsertTo(direction: Direction): Boolean
        fun canExtractFrom(direction: Direction): Boolean
        val shouldDropItem: Boolean
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

    override fun toInitialChunkDataNbt(): NbtCompound = createNbt() // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this) // TODO スロットの更新はカスタムパケットに分けるのでこちらはオーバーライドしない


    // Inventory

    /**
     * スロットの内容が変化する際に呼び出されます。
     * このイベントはスロットの更新が行われた後に呼び出されることは保証されません。
     */
    open fun onStackChange(slot: Int?) {
        // TODO スロットアップデートのための軽量カスタムパケット
        if (slot == null || slot in animationManager.animationSlots) {
            world?.updateListeners(pos, cachedState, cachedState, Block.NOTIFY_ALL)
        }
    }

    open fun getHorizontalFacing(): Direction? = null

    private val inventory = MutableList(arguments.configuration.slotConfigurations.size) { EMPTY_ITEM_STACK }

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

    override fun isValid(slot: Int, stack: ItemStack) = arguments.configuration.slotConfigurations[slot].filter(stack)

    private fun getActualSide(side: Direction): Direction {
        if (side == Direction.UP) return side
        if (side == Direction.DOWN) return side
        val horizontalFacing = getHorizontalFacing() ?: return side
        return Direction.fromHorizontal((horizontalFacing.horizontal + side.horizontal) % 4)
    }

    private val availableSlotsTable = Direction.entries.map { direction ->
        arguments.configuration.slotConfigurations.withIndex().filter { it.value.canInsertTo(direction) || it.value.canExtractFrom(direction) }.map { it.index }.toIntArray()
    }.toTypedArray()

    override fun getAvailableSlots(side: Direction) = availableSlotsTable[getActualSide(side).id]

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?) = (dir == null || arguments.configuration.slotConfigurations[slot].canInsertTo(getActualSide(dir))) && isValid(slot, stack)

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction) = arguments.configuration.slotConfigurations[slot].canExtractFrom(getActualSide(dir))

    override fun clear() {
        onStackChange(null)
        markDirty()
        inventory.replaceAll { EMPTY_ITEM_STACK }
    }

    fun dropItems() {
        inventory.forEachIndexed { index, itemStack ->
            if (arguments.configuration.slotConfigurations[index].shouldDropItem) ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack)
        }
        onStackChange(null)
        markDirty()
    }


    // Move

    open fun serverTick(world: World, pos: BlockPos, state: BlockState) = Unit

    open fun getComparatorOutput() = 0


    // Rendering

    open fun clientTick(world: World, pos: BlockPos, state: BlockState) {
        animationManager.animators.forEach {
            it.tick(world.random)
        }
    }

    interface AnimationConfiguration {
        fun getItemStack(): ItemStack
        val motion: Motion
        val positions: List<Position>
        fun getSpeed(): Double
        val slotIndex: Int?
    }

    enum class Motion {
        NONE,
        FAIRY,
    }

    /**
     * @param x 1/16 scale
     * @param y 1/16 scale
     * @param z 1/16 scale
     * @param pitch degree
     * @param yaw degree
     */
    class Position(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float, val duration: Double)

    open fun createAnimationConfigurations(): List<AnimationConfiguration> = listOf()

    private val animationManager by lazy { AnimationManager(createAnimationConfigurations()) }

    private class AnimationManager(animationConfigurations: List<AnimationConfiguration>) {
        val animationSlots = animationConfigurations.map { it.slotIndex }.toSet()
        val animators = animationConfigurations.map { Animator(it) }
    }

    private class Animator(val animationConfiguration: AnimationConfiguration) {
        init {
            check(animationConfiguration.positions.isNotEmpty())
        }

        private var index = 0
        private var position = animationConfiguration.positions[index]
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

        fun tick(random: Random) {

            // 定位置の切り替え
            val speed = animationConfiguration.getSpeed()
            if (speed > 0) {
                countdown -= speed
                if (countdown <= 0) {

                    index++
                    if (index >= animationConfiguration.positions.size) index = 0

                    position = animationConfiguration.positions[index]
                    countdown = animationConfiguration.positions[index].duration * (1.0 + random.nextDouble() * 0.1)

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

    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val horizontal = getHorizontalFacing()?.horizontal ?: 0

        renderingProxy.stack {
            renderingProxy.translate(0.5, 0.5, 0.5)
            renderingProxy.rotateY(-((horizontal + 2) * 90) / 180F * Math.PI.toFloat())
            renderingProxy.translate(-0.5, -0.5, -0.5)

            animationManager.animators.forEachIndexed { index, animator ->

                val x = animator.x + animator.xSpeed * tickDelta.toDouble()
                val y = animator.y + animator.ySpeed * tickDelta.toDouble()
                val z = animator.z + animator.zSpeed * tickDelta.toDouble()
                val yaw = animator.yaw + animator.yawSpeed * tickDelta
                val pitch = animator.pitch + animator.pitchSpeed * tickDelta
                val yawOffset = when (animator.animationConfiguration.motion) {
                    Motion.FAIRY -> MathHelper.sin((animator.ticks.toFloat() + tickDelta) * 0.03F) * 3F
                    Motion.NONE -> 0F
                }
                val pitchOffset = when (animator.animationConfiguration.motion) {
                    Motion.FAIRY -> MathHelper.sin((animator.ticks.toFloat() + tickDelta) * 0.08F) * 5F
                    Motion.NONE -> 0F
                }

                renderingProxy.stack {
                    renderingProxy.translate(x / 16.0, y / 16.0, z / 16.0) // 移動
                    renderingProxy.rotateY(-yaw / 180F * MathHelper.PI) // 横回転
                    renderingProxy.rotateX(-pitch / 180F * MathHelper.PI) // 足元を起点にして縦回転
                    renderingProxy.scale(0.5F, 0.5F, 0.5F) // 縮小

                    when (animator.animationConfiguration.motion) {
                        Motion.FAIRY -> {
                            renderingProxy.translate(0.0, 0.25, 0.0)
                            renderingProxy.rotateY(-yawOffset / 180F * MathHelper.PI) // 横回転
                            renderingProxy.rotateZ(-pitchOffset / 180F * MathHelper.PI) // 上下回転
                            renderingProxy.translate(0.0, -0.25, 0.0)
                        }

                        Motion.NONE -> Unit
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

    open fun createProperties(): List<SimpleMachineScreenHandler.Property> = listOf()

    abstract fun createScreenHandler(screenHandlerArguments: SimpleMachineScreenHandler.Arguments): ScreenHandler

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        val screenHandlerArguments = SimpleMachineScreenHandler.Arguments(
            arguments.configuration.screenHandlerConfiguration,
            syncId,
            playerInventory,
            this,
            createProperties(),
            ScreenHandlerContext.create(world, pos),
        )
        return createScreenHandler(screenHandlerArguments)
    }

}

abstract class FairyBuildingBlockEntity<C : FairyBuildingCard<C, *, *, E, *>, E : FairyBuildingBlockEntity<C, E>>(val card: C, pos: BlockPos, state: BlockState) :
    SimpleMachineBlockEntity(Arguments(object : Configuration {
        override val type = card.blockEntityType
        override val slotConfigurations = card.slotConfigurations
        override val screenHandlerConfiguration = card.screenHandlerConfiguration
    }, pos, state)) {

    abstract val self: E

    override fun getHorizontalFacing() = cachedState.getOrNull(HorizontalFacingBlock.FACING) ?: Direction.NORTH

    override fun getContainerName(): Text = card.block.name

    override fun createProperties() = card.createProperties(self)

    override fun createAnimationConfigurations(): List<AnimationConfiguration> = card.slotConfigurations.mapIndexedNotNull { slotIndex, slotConfiguration ->
        val slotAnimationConfiguration = slotConfiguration.animation ?: return@mapIndexedNotNull null
        object : AnimationConfiguration {
            override fun getItemStack() = getStack(slotIndex)
            override val motion = slotAnimationConfiguration.motion
            override val positions = slotAnimationConfiguration.positions
            override fun getSpeed() = a
            override val slotIndex = slotIndex
        }
    }

    override fun createScreenHandler(screenHandlerArguments: SimpleMachineScreenHandler.Arguments) = card.createScreenHandler(screenHandlerArguments)

}

open class FairyBuildingScreenHandler<C : FairyBuildingCard<C, *, *, *, *>>(val card: C, arguments: Arguments) :
    SimpleMachineScreenHandler(arguments) {

    override fun canUse(player: PlayerEntity) = canUse(arguments.context, player, card.block)

}
