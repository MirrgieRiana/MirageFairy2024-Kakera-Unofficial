package miragefairy2024.mod.fairyhouse

import miragefairy2024.RenderingProxy
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaLeavesBlock
import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.get
import miragefairy2024.util.getOrNull
import miragefairy2024.util.int
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
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
    { blockCreator(it.luminance { blockState -> if (blockState.getOrNull(FairyFactoryBlock.LIT) == true) 5 else 0 }) },
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
        val FOLIA_PROPERTY = AbstractFairyHouseBlockEntity.PropertySettings<FairyFactoryBlockEntity<*>>({ folia }, { folia = it })
    }
}

open class FairyFactoryBlock(cardGetter: () -> FairyFactoryCard<*, *>, settings: Settings) : AbstractFairyHouseBlock(cardGetter, settings) {
    companion object {
        val LIT: BooleanProperty = Properties.LIT
    }

    init {
        defaultState = defaultState.with(LIT, false)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(LIT)
    }
}

abstract class FairyFactoryBlockEntity<E : FairyFactoryBlockEntity<E>>(card: FairyFactoryCard<E, *>, pos: BlockPos, state: BlockState) : AbstractFairyHouseBlockEntity<E>(card, pos, state) {

    var folia = 0
    private var lastCollectTime = 0L

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        folia = nbt.wrapper["Folia"].int.get() ?: 0
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["Folia"].int.set(folia)
    }

    override fun tick(world: World, pos: BlockPos, state: BlockState) {
        super.tick(world, pos, state)
        if (folia < 1000) {
            if (lastCollectTime == 0L) lastCollectTime = world.time - world.random.nextInt(200)
            if (world.time >= lastCollectTime + 100L) {
                lastCollectTime = world.time

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

    override fun renderExtra(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        val i = (light and 0x0000FF) or 0xF00000
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
