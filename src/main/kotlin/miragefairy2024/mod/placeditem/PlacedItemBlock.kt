package miragefairy2024.mod.placeditem

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.RenderingProxy
import miragefairy2024.RenderingProxyBlockEntity
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.compound
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.double
import miragefairy2024.util.get
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRenderingProxyBlockEntityRendererFactory
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.toNbt
import miragefairy2024.util.with
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.castOrNull
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.data.client.TextureKey
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

object PlacedItemCard {
    val identifier = Identifier(MirageFairy2024.modId, "placed_item")
    val block = PlacedItemBlock(AbstractBlock.Settings.create().noCollision().strength(0.2F).pistonBehavior(PistonBehavior.DESTROY))
    val blockEntityType = BlockEntityType(::PlacedItemBlockEntity, setOf(block), null)
}

context(ModContext)
fun initPlacedItemBlock() {
    PlacedItemCard.let { card ->
        card.block.register(Registries.BLOCK, card.identifier)
        card.blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, card.identifier)

        card.block.registerSingletonBlockStateGeneration()
        card.block.registerModelGeneration {
            Model {
                ModelData(
                    parent = Identifier("minecraft", "block/block"),
                    textures = ModelTexturesData(
                        TextureKey.PARTICLE.name to Identifier("minecraft", "block/glass").string,
                    ),
                    elements = ModelElementsData(),
                )
            }.with()
        }
        card.blockEntityType.registerRenderingProxyBlockEntityRendererFactory()

        card.block.registerBlockTagGeneration { BlockTags.HOE_MINEABLE }
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class PlacedItemBlock(settings: Settings) : Block(settings), BlockEntityProvider {
    companion object {
        private val SHAPE: VoxelShape = createCuboidShape(6.0, 2.0)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = PlacedItemBlockEntity(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    // レンダリング
    override fun getRenderType(state: BlockState) = BlockRenderType.ENTITYBLOCK_ANIMATED
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = SHAPE

    // 真下が空気だと壊れる
    override fun canPlaceAt(state: BlockState?, world: WorldView, pos: BlockPos) = !world.isAir(pos.down())
    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
        if (!state.canPlaceAt(world, pos)) return Blocks.AIR.defaultState
        @Suppress("DEPRECATION")
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    // 格納されているアイテムをドロップする
    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState) = world.getBlockEntity(pos).castOrNull<PlacedItemBlockEntity>()?.itemStack ?: EMPTY_ITEM_STACK
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) run {
            val blockEntity = world.getBlockEntity(pos) as? PlacedItemBlockEntity ?: return@run
            dropStack(world, pos, blockEntity.itemStack)
        }
        @Suppress("DEPRECATION")
        super.onStateReplaced(state, world, pos, newState, moved)
    }

}

class PlacedItemBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(PlacedItemCard.blockEntityType, pos, state), RenderingProxyBlockEntity {
    companion object {
        private val INVALID_ITEM_STACK = Items.BARRIER.createItemStack()
    }


    var itemStack = EMPTY_ITEM_STACK
    var itemX = 0.5
    var itemY = 0.5 / 16.0
    var itemZ = 0.5
    var itemRotateX = MathHelper.TAU * 0.25
    var itemRotateY = 0.0


    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["ItemStack"].set(itemStack.toNbt())
        nbt.wrapper["ItemX"].double.set(itemX)
        nbt.wrapper["ItemY"].double.set(itemY)
        nbt.wrapper["ItemZ"].double.set(itemZ)
        nbt.wrapper["ItemRotateX"].double.set(itemRotateX)
        nbt.wrapper["ItemRotateY"].double.set(itemRotateY)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        itemStack = ItemStack.fromNbt(nbt.wrapper["ItemStack"].compound.get())
        itemX = nbt.wrapper["ItemX"].double.get() ?: 0.0
        itemY = nbt.wrapper["ItemY"].double.get() ?: 0.0
        itemZ = nbt.wrapper["ItemZ"].double.get() ?: 0.0
        itemRotateX = nbt.wrapper["ItemRotateX"].double.get() ?: 0.0
        itemRotateY = nbt.wrapper["ItemRotateY"].double.get() ?: 0.0
    }

    override fun toInitialChunkDataNbt(): NbtCompound = createNbt()
    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)


    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        renderingProxy.stack {
            renderingProxy.translate(itemX, itemY, itemZ)
            renderingProxy.rotateY(itemRotateY.toFloat())
            renderingProxy.rotateX(itemRotateX.toFloat())
            renderingProxy.renderItemStack(if (itemStack.isEmpty) INVALID_ITEM_STACK else itemStack)
        }
    }

}
