package miragefairy2024.mod.fairybuilding

import miragefairy2024.RenderingProxy
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.fairy.contains
import miragefairy2024.mod.fairy.getFairyCondensation
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.cards.HaimeviskaLeavesBlock
import miragefairy2024.util.NeighborType
import miragefairy2024.util.blockVisitor
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.EnumProperty
import kotlin.math.log
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.util.StringRepresentable as StringIdentifiable
import net.minecraft.world.level.block.state.StateDefinition as StateManager

abstract class FairyFactoryCard<B : FairyFactoryBlock, E : FairyFactoryBlockEntity<E>, H : FairyFactoryScreenHandler> : FairyBuildingCard<B, E, H>() {
    companion object {
        val FOLIA_PROPERTY = PropertyConfiguration<FairyFactoryBlockEntity<*>>({ folia }, { folia = it }, { (it / 10).toShort() }, { it.toInt() * 10 })

        fun isFairy(itemStack: ItemStack, motif: Motif): Boolean {
            if (!itemStack.`is`(FairyCard.item())) return false
            val childMotif = itemStack.getFairyMotif() ?: return false
            return childMotif in motif
        }
    }

    override fun createBlockSettings(): FabricBlockSettings = super.createBlockSettings().lightLevel { blockState -> if (blockState.getValue(FairyFactoryBlock.STATUS).isLit) 8 else 0 }

    override fun createPropertyConfigurations() = super.createPropertyConfigurations() + FOLIA_PROPERTY

    abstract val collectingFolia: Int
    abstract val maxFolia: Int
}

abstract class FairyFactoryBlock(card: FairyFactoryCard<*, *, *>) : FairyBuildingBlock(card) {
    companion object {
        val STATUS: EnumProperty<Status> = EnumProperty.create("status", Status::class.java)
    }

    enum class Status(private val string: String, val isLit: Boolean, val doMovePosition: Boolean) : StringIdentifiable {
        OFFLINE("offline", false, false),
        IDLE("idle", true, false),
        PROCESSING("processing", true, true),
        ;

        override fun getSerializedName() = string
    }

    init {
        registerDefaultState(defaultBlockState().setValue(STATUS, Status.OFFLINE))
    }

    override fun createBlockStateDefinition(builder: StateManager.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(STATUS)
    }
}

abstract class FairyFactoryBlockEntity<E : FairyFactoryBlockEntity<E>>(private val card: FairyFactoryCard<*, E, *>, pos: BlockPos, state: BlockState) : FairyBuildingBlockEntity<E>(card, pos, state) {
    companion object {
        fun getFairyLevel(itemStack: ItemStack): Double {
            if (!itemStack.`is`(FairyCard.item())) return 0.0
            val motif = itemStack.getFairyMotif() ?: return 0.0
            val count = itemStack.getFairyCondensation() * itemStack.count
            val level = motif.rare.toDouble() + log(count.toDouble(), 3.0)
            return level
        }
    }

    fun setStatus(status: FairyFactoryBlock.Status) {
        val world = level ?: return
        if (blockState.getValue(FairyFactoryBlock.STATUS) != status) {
            world.setBlock(worldPosition, blockState.setValue(FairyFactoryBlock.STATUS, status), Block.UPDATE_ALL)
        }
    }


    override fun loadAdditional(nbt: NbtCompound, registries: HolderLookup.Provider) {
        super.loadAdditional(nbt, registries)
        folia = nbt.wrapper["Folia"].int.get() ?: 0
        foliaCollectionCooldown = nbt.wrapper["FoliaCollectionCooldown"].int.get() ?: 0
    }

    override fun saveAdditional(nbt: NbtCompound, registries: HolderLookup.Provider) {
        super.saveAdditional(nbt, registries)
        nbt.wrapper["Folia"].int.set(folia)
        nbt.wrapper["FoliaCollectionCooldown"].int.set(foliaCollectionCooldown)
    }


    var folia = 0
    private var foliaCollectionCooldown = 0

    override fun serverTick(world: Level, pos: BlockPos, state: BlockState) {
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
        val world = level ?: return

        // 最大200ブロックのハイメヴィスカの原木を探す
        val logs = blockVisitor(listOf(worldPosition), maxCount = 200, neighborType = NeighborType.VERTICES) { _, _, toBlockPos ->
            world.getBlockState(toBlockPos).`is`(FAIRY_BUILDING_BLOCK_TAG)
        }.map { it.second }.toList()

        // 最大距離6までの葉をすべて探す
        var changed = false
        run finished@{
            blockVisitor(logs, visitOrigins = false, maxDistance = 6) { _, _, toBlockPos ->
                world.getBlockState(toBlockPos).`is`(HaimeviskaBlockCard.LEAVES.block())
            }.forEach { (_, blockPos) ->
                val blockState = world.getBlockState(blockPos)
                if (blockState.getValue(HaimeviskaLeavesBlock.CHARGED)) {
                    folia += 1000
                    changed = true
                    world.setBlock(blockPos, blockState.setValue(HaimeviskaLeavesBlock.CHARGED, false), Block.UPDATE_CLIENTS)
                    if (folia >= card.maxFolia) return@finished
                }
            }
        }
        if (changed) setChanged()

    }


    override val doMovePosition get() = blockState.getValue(FairyFactoryBlock.STATUS).doMovePosition

    override fun renderRotated(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        super.renderRotated(renderingProxy, tickDelta, light, overlay)
        if (blockState.getValue(FairyFactoryBlock.STATUS).isLit) {
            renderingProxy.renderCutoutBlock(FairyBuildingModelCard.LANTERN.identifier, null, 1.0F, 1.0F, 1.0F, (light and 0x0000FF) or 0xF00000, overlay)
        } else {
            renderingProxy.renderCutoutBlock(FairyBuildingModelCard.LANTERN_OFF.identifier, null, 1.0F, 1.0F, 1.0F, light, overlay)
        }
    }

}

open class FairyFactoryScreenHandler(card: FairyFactoryCard<*, *, *>, arguments: Arguments) : FairyBuildingScreenHandler(card, arguments) {
    var folia by Property(FairyFactoryCard.FOLIA_PROPERTY)
}
