package miragefairy2024.mod.fairyhouse

import miragefairy2024.RenderingProxy
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaLeavesBlock
import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class FairyFactoryCard<E : FairyFactoryBlockEntity<E>, H : FairyFactoryScreenHandler>(
    path: String,
    tier: Int,
    enName: String,
    jaName: String,
    enPoem: String,
    jaPoem: String,
    blockCreator: (FabricBlockSettings) -> FairyFactoryBlock,
    blockEntityAccessor: BlockEntityAccessor<E>,
    screenHandlerCreator: (AbstractFairyHouseScreenHandler.Arguments) -> H,
    guiWidth: Int,
    guiHeight: Int,
    oldBlockEntitySettings: AbstractFairyHouseBlockEntity.Settings<E>,
) : AbstractFairyHouseCard<E, H>(
    path,
    tier,
    enName,
    jaName,
    enPoem,
    jaPoem,
    { blockCreator(it.luminance { blockState -> if (blockState[FairyFactoryBlock.STATUS].isLit) 5 else 0 }) },
    blockEntityAccessor,
    screenHandlerCreator,
    guiWidth,
    guiHeight,
    AbstractFairyHouseBlockEntity.Settings(
        oldBlockEntitySettings.slots,
        listOf(FOLIA_PROPERTY) + oldBlockEntitySettings.properties,
    ),
) {
    companion object {
        val FOLIA_PROPERTY = AbstractFairyHouseBlockEntity.PropertySettings<FairyFactoryBlockEntity<*>>({ getFolia() }, { setFolia(it) })
    }
}

open class FairyFactoryBlock(cardGetter: () -> FairyFactoryCard<*, *>, settings: Settings) : AbstractFairyHouseBlock(cardGetter, settings) {
    companion object {
        val STATUS: EnumProperty<Status> = EnumProperty.of("status", Status::class.java)
    }

    enum class Status(private val string: String, val isLit: Boolean) : StringIdentifiable {
        OFFLINE("offline", false),
        IDLE("idle", true),
        PROCESSING("processing", true),
        ;

        override fun asString() = string
    }

    init {
        defaultState = defaultState.with(STATUS, Status.OFFLINE)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(STATUS)
    }
}

abstract class FairyFactoryBlockEntity<E : FairyFactoryBlockEntity<E>>(card: FairyFactoryCard<E, *>, pos: BlockPos, state: BlockState) : AbstractFairyHouseBlockEntity<E>(card, pos, state) {

    fun setStatus(status: FairyFactoryBlock.Status) {
        val world = world ?: return
        if (cachedState[FairyFactoryBlock.STATUS] != status) {
            world.setBlockState(pos, cachedState.with(FairyFactoryBlock.STATUS, status), Block.NOTIFY_ALL)
        }
    }


    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        folia = nbt.wrapper["Folia"].int.get() ?: 0
        collectionCooldown = nbt.wrapper["CollectionCooldown"].int.get() ?: 0
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["Folia"].int.set(folia)
        nbt.wrapper["CollectionCooldown"].int.set(collectionCooldown)
    }


    private var folia = 0

    fun getFolia() = folia

    fun setFolia(folia: Int) {
        val oldFolia = this.folia
        if (folia != oldFolia) {
            this.folia = folia
            onFoliaChanged(oldFolia, folia)
        }
    }

    open fun onFoliaChanged(oldFolia: Int, newFolia: Int) = Unit


    private var collectionCooldown = 0

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)
        if (collectionCooldown > 0) {
            collectionCooldown--
        } else {
            if (folia < 1000) {
                collectionCooldown = 200
                collectFolia()
            }
        }
    }

    private fun collectFolia() {
        val world = world ?: return

        // 最大200ブロックのハイメヴィスカの原木を探す
        val logs = blockVisitor(listOf(pos), maxCount = 200, neighborType = NeighborType.VERTICES) { _, toBlockPos ->
            world.getBlockState(toBlockPos).isIn(HAIMEVISKA_LOGS)
        }.map { it.second }.toList()

        // 最大距離6までの葉をすべて探す
        var changed = false
        var newFolia = folia
        run finished@{
            blockVisitor(logs, visitOrigins = false, maxDistance = 6) { _, toBlockPos ->
                world.getBlockState(toBlockPos).isOf(HaimeviskaBlockCard.LEAVES.block)
            }.forEach { (_, blockPos) ->
                val blockState = world.getBlockState(blockPos)
                if (blockState[HaimeviskaLeavesBlock.CHARGED]) {
                    newFolia += 100
                    changed = true
                    world.setBlockState(blockPos, blockState.with(HaimeviskaLeavesBlock.CHARGED, false), Block.NOTIFY_LISTENERS)
                    if (newFolia > 10000) return@finished
                }
            }
        }
        if (changed) {
            setFolia(newFolia)
            markDirty()
        }

    }


    override fun renderExtra(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val i = if (cachedState[FairyFactoryBlock.STATUS].isLit) (light and 0x0000FF) or 0xF00000 else light
        renderingProxy.renderCutoutBlock(FairyHouseModelCard.LANTERN.identifier, null, 1.0F, 1.0F, 1.0F, i, overlay)
    }

}

open class FairyFactoryScreenHandler(card: FairyFactoryCard<*, *>, arguments: Arguments) : AbstractFairyHouseScreenHandler(card, arguments) {

    var folia: Int
        get() = arguments.propertyDelegate.get(card.propertyIndexTable[FairyFactoryCard.FOLIA_PROPERTY]!!)
        set(value) {
            arguments.propertyDelegate.set(card.propertyIndexTable[FairyFactoryCard.FOLIA_PROPERTY]!!, value)
        }

}
