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
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.log

abstract class FairyFactoryConfiguration<C : FairyFactoryCard<C, S, B, E, H>, S : FairyFactoryConfiguration<C, S, B, E, H>, B : FairyFactoryBlock, E : FairyFactoryBlockEntity<E>, H : FairyFactoryScreenHandler> : FairyBuildingConfiguration<C, S, B, E, H>() {
    companion object {
        val FOLIA_PROPERTY = FairyBuildingPropertyConfiguration<FairyFactoryBlockEntity<*>>({ folia }, { folia = it }, { (it / 10).toShort() }, { it * 10 })

        fun isFairy(itemStack: ItemStack, motif: Motif): Boolean {
            if (!itemStack.isOf(FairyCard.item)) return false
            val childMotif = itemStack.getFairyMotif() ?: return false
            return childMotif in motif
        }
    }

    override fun createBlockSettings(): FabricBlockSettings = super.createBlockSettings().luminance { blockState -> if (blockState[FairyFactoryBlock.STATUS].isLit) 8 else 0 }

    override fun createPropertyConfigurations() = super.createPropertyConfigurations() + FOLIA_PROPERTY

    abstract val collectingFolia: Int
    abstract val maxFolia: Int
}

open class FairyFactoryCard<C : FairyFactoryCard<C, S, B, E, H>, S : FairyFactoryConfiguration<C, S, B, E, H>, B : FairyFactoryBlock, E : FairyFactoryBlockEntity<E>, H : FairyFactoryScreenHandler>(settings: S) : FairyBuildingCard<C, S, B, E, H>(settings)

open class FairyFactoryBlock(cardGetter: () -> FairyFactoryCard<*, *, *, *, *>, settings: Settings) : FairyBuildingBlock(cardGetter, settings) {
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
        defaultState = defaultState.with(STATUS, Status.OFFLINE)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(STATUS)
    }
}

abstract class FairyFactoryBlockEntity<E : FairyFactoryBlockEntity<E>>(private val card: FairyFactoryCard<*, *, *, E, *>, pos: BlockPos, state: BlockState) :
    FairyBuildingBlockEntity<E>(card, pos, state) {
    companion object {
        fun getFairyLevel(itemStack: ItemStack): Double {
            if (!itemStack.isOf(FairyCard.item)) return 0.0
            val motif = itemStack.getFairyMotif() ?: return 0.0
            val count = itemStack.getFairyCondensation() * itemStack.count
            val level = motif.rare.toDouble() + log(count.toDouble(), 3.0)
            return level
        }
    }

    fun setStatus(status: FairyFactoryBlock.Status) {
        val world = world ?: return
        if (cachedState[FairyFactoryBlock.STATUS] != status) {
            world.setBlockState(pos, cachedState.with(FairyFactoryBlock.STATUS, status), Block.NOTIFY_ALL)
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
            if (folia < card.configuration.collectingFolia) {
                foliaCollectionCooldown = 200
                collectFolia()
            }
        }
    }

    private fun collectFolia() {
        val world = world ?: return

        // 最大200ブロックのハイメヴィスカの原木を探す
        val logs = blockVisitor(listOf(pos), maxCount = 200, neighborType = NeighborType.VERTICES) { _, _, toBlockPos ->
            world.getBlockState(toBlockPos).isIn(HAIMEVISKA_LOGS)
        }.map { it.second }.toList()

        // 最大距離6までの葉をすべて探す
        var changed = false
        run finished@{
            blockVisitor(logs, visitOrigins = false, maxDistance = 6) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).isOf(HaimeviskaBlockCard.LEAVES.block)
            }.forEach { (_, blockPos) ->
                val blockState = world.getBlockState(blockPos)
                if (blockState[HaimeviskaLeavesBlock.CHARGED]) {
                    folia += 1000
                    changed = true
                    world.setBlockState(blockPos, blockState.with(HaimeviskaLeavesBlock.CHARGED, false), Block.NOTIFY_LISTENERS)
                    if (folia >= card.configuration.maxFolia) return@finished
                }
            }
        }
        if (changed) markDirty()

    }


    override val doMovePosition get() = cachedState[FairyFactoryBlock.STATUS].doMovePosition

    override fun renderExtra(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        if (cachedState[FairyFactoryBlock.STATUS].isLit) {
            renderingProxy.renderCutoutBlock(FairyBuildingModelCard.LANTERN.identifier, null, 1.0F, 1.0F, 1.0F, (light and 0x0000FF) or 0xF00000, overlay)
        } else {
            renderingProxy.renderCutoutBlock(FairyBuildingModelCard.LANTERN_OFF.identifier, null, 1.0F, 1.0F, 1.0F, light, overlay)
        }
    }

}

open class FairyFactoryScreenHandler(card: FairyFactoryCard<*, *, *, *, *>, arguments: Arguments) : FairyBuildingScreenHandler(card, arguments) {
    var folia by Property(FairyFactoryConfiguration.FOLIA_PROPERTY)
}
