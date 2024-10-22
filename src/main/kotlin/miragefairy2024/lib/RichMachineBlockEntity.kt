package miragefairy2024.lib

import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.reset
import miragefairy2024.util.writeToNbt
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
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
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

abstract class RichMachineBlockEntity(val arguments: Arguments) : LockableContainerBlockEntity(arguments.configuration.type, arguments.pos, arguments.state), SidedInventory, RenderingProxyBlockEntity {

    class Arguments(
        val configuration: Configuration,
        val pos: BlockPos,
        val state: BlockState,
    )

    interface Configuration {
        val type: BlockEntityType<*>
        val slotConfigurations: List<SlotConfiguration>
        val screenHandlerConfiguration: RichMachineScreenHandler.Configuration
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

    open fun getHorizontalFacing(): Direction? = null

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
        fun getMotion(): Motion
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
                val yawOffset = when (animator.animationConfiguration.getMotion()) {
                    Motion.FAIRY -> MathHelper.sin((animator.ticks.toFloat() + tickDelta) * 0.03F) * 3F
                    Motion.NONE -> 0F
                }
                val pitchOffset = when (animator.animationConfiguration.getMotion()) {
                    Motion.FAIRY -> MathHelper.sin((animator.ticks.toFloat() + tickDelta) * 0.08F) * 5F
                    Motion.NONE -> 0F
                }

                renderingProxy.stack {
                    renderingProxy.translate(x / 16.0, y / 16.0, z / 16.0) // 移動
                    renderingProxy.rotateY(-yaw / 180F * MathHelper.PI) // 横回転
                    renderingProxy.rotateX(-pitch / 180F * MathHelper.PI) // 足元を起点にして縦回転
                    renderingProxy.scale(0.5F, 0.5F, 0.5F) // 縮小

                    when (animator.animationConfiguration.getMotion()) {
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

    open fun createProperties(): List<RichMachineScreenHandler.Property> = listOf()

    abstract fun createScreenHandler(screenHandlerArguments: RichMachineScreenHandler.Arguments): ScreenHandler

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        val screenHandlerArguments = RichMachineScreenHandler.Arguments(
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
