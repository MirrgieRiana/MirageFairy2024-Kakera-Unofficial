package miragefairy2024.mod.fairybuilding

import miragefairy2024.RenderingProxy
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.fairy.contains
import miragefairy2024.mod.fairy.getFairyCondensation
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaLeavesBlock
import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.world.level.block.state.StateDefinition as StateManager
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.util.StringRepresentable as StringIdentifiable
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World
import kotlin.math.log

abstract class FairyFactoryCard<B : FairyFactoryBlock, E : FairyFactoryBlockEntity<E>, H : FairyFactoryScreenHandler> : FairyBuildingCard<B, E, H>() {
    companion object {
        val FOLIA_PROPERTY = PropertyConfiguration<FairyFactoryBlockEntity<*>>({ folia }, { folia = it }, { (it / 10).toShort() }, { it.toInt() * 10 })

        fun isFairy(itemStack: ItemStack, motif: Motif): Boolean {
            if (!itemStack.`is`(FairyCard.item)) return false
            val childMotif = itemStack.getFairyMotif() ?: return false
            return childMotif in motif
        }
    }

    override fun createBlockSettings(): FabricBlockSettings = super.createBlockSettings().luminance { blockState -> if (blockState[FairyFactoryBlock.STATUS].isLit) 8 else 0 }

    override fun createPropertyConfigurations() = super.createPropertyConfigurations() + FOLIA_PROPERTY

    abstract val collectingFolia: Int
    abstract val maxFolia: Int
}

open class FairyFactoryBlock(card: FairyFactoryCard<*, *, *>) : FairyBuildingBlock(card) {
    companion object {
        val STATUS: EnumProperty<Status> = EnumProperty.of("status", Status::class.java)
    }

    enum class Status(private val string: String, val isLit: Boolean, val doMovePosition: Boolean) : StringIdentifiable {
        OFFLINE("offline", false, false),
        IDLE("idle", true, false),
        PROCESSING("processing", true, true),
        ;

        override fun asString() = string
    }

    init {
        defaultBlockState = defaultBlockState.setValue(STATUS, Status.OFFLINE)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(STATUS)
    }
}

abstract class FairyFactoryBlockEntity<E : FairyFactoryBlockEntity<E>>(private val card: FairyFactoryCard<*, E, *>, pos: BlockPos, state: BlockState) : FairyBuildingBlockEntity<E>(card, pos, state) {
    companion object {
        fun getFairyLevel(itemStack: ItemStack): Double {
            if (!itemStack.`is`(FairyCard.item)) return 0.0
            val motif = itemStack.getFairyMotif() ?: return 0.0
            val count = itemStack.getFairyCondensation() * itemStack.count
            val level = motif.rare.toDouble() + log(count.toDouble(), 3.0)
            return level
        }
    }

    fun setStatus(status: FairyFactoryBlock.Status) {
        val world = world ?: return
        if (cachedState[FairyFactoryBlock.STATUS] != status) {
            world.setBlockState(pos, cachedState.setValue(FairyFactoryBlock.STATUS, status), Block.UPDATE_ALL)
        }
    }


    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        folia = nbt.wrapper["Folia"].int.get() ?: 0
        foliaCollectionCooldown = nbt.wrapper["FoliaCollectionCooldown"].int.get() ?: 0
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["Folia"].int.set(folia)
        nbt.wrapper["FoliaCollectionCooldown"].int.set(foliaCollectionCooldown)
    }


    var folia = 0
    private var foliaCollectionCooldown = 0

    override fun serverTick(world: World, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)
        if (foliaCollectionCooldown > 0) {
            foliaCollectionCooldown--
        } else {
            if (folia < card.collectingFolia) {
                foliaCollectionCooldown = 200
                collectFolia()
            }
        }
    }

    private fun collectFolia() {
        val world = world ?: return

        // 最大200ブロックのハイメヴィスカの原木を探す
        val logs = blockVisitor(listOf(pos), maxCount = 200, neighborType = NeighborType.VERTICES) { _, _, toBlockPos ->
            world.getBlockState(toBlockPos).`is`(HAIMEVISKA_LOGS)
        }.map { it.second }.toList()

        // 最大距離6までの葉をすべて探す
        var changed = false
        run finished@{
            blockVisitor(logs, visitOrigins = false, maxDistance = 6) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).`is`(HaimeviskaBlockCard.LEAVES.block)
            }.forEach { (_, blockPos) ->
                val blockState = world.getBlockState(blockPos)
                if (blockState[HaimeviskaLeavesBlock.CHARGED]) {
                    folia += 1000
                    changed = true
                    world.setBlockState(blockPos, blockState.setValue(HaimeviskaLeavesBlock.CHARGED, false), Block.NOTIFY_LISTENERS)
                    if (folia >= card.maxFolia) return@finished
                }
            }
        }
        if (changed) markDirty()

    }


    override val doMovePosition get() = cachedState[FairyFactoryBlock.STATUS].doMovePosition

    override fun renderRotated(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        super.renderRotated(renderingProxy, tickDelta, light, overlay)
        if (cachedState[FairyFactoryBlock.STATUS].isLit) {
            renderingProxy.renderCutoutBlock(FairyBuildingModelCard.LANTERN.identifier, null, 1.0F, 1.0F, 1.0F, (light and 0x0000FF) or 0xF00000, overlay)
        } else {
            renderingProxy.renderCutoutBlock(FairyBuildingModelCard.LANTERN_OFF.identifier, null, 1.0F, 1.0F, 1.0F, light, overlay)
        }
    }

}

open class FairyFactoryScreenHandler(card: FairyFactoryCard<*, *, *>, arguments: Arguments) : FairyBuildingScreenHandler(card, arguments) {
    var folia by Property(FairyFactoryCard.FOLIA_PROPERTY)
}
