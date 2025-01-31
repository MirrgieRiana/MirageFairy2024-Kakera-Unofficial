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
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import mirrg.kotlin.hydrogen.castOrNull
import mirrg.kotlin.hydrogen.max
import mirrg.kotlin.hydrogen.min
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
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
import net.minecraft.util.math.MathHelper
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

object PlacedItemCard {
    val identifier = MirageFairy2024.identifier("placed_item")
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

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = PlacedItemBlockEntity(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onSyncedBlockEvent(state: BlockState, world: World, pos: BlockPos, type: Int, data: Int): Boolean {
        super.onSyncedBlockEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.onSyncedBlockEvent(type, data)
    }

    // レンダリング
    override fun getRenderType(state: BlockState) = BlockRenderType.ENTITYBLOCK_ANIMATED
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        val blockEntity = world.getBlockEntity(pos) as? PlacedItemBlockEntity ?: return VoxelShapes.fullCube()
        return blockEntity.shapeCache ?: VoxelShapes.fullCube()
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

// TODO 右クリックで立てたりする
class PlacedItemBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(PlacedItemCard.blockEntityType, pos, state), RenderingProxyBlockEntity {
    companion object {
        private val INVALID_ITEM_STACK = Items.BARRIER.createItemStack()
    }


    var itemStack = EMPTY_ITEM_STACK
    var itemX = 8.0 / 16.0
    var itemY = 0.5 / 16.0
    var itemZ = 8.0 / 16.0
    var itemRotateX = -MathHelper.TAU * 0.25
    var itemRotateY = 0.0
    var shapeCache: VoxelShape? = null


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
        updateShapeCache()
    }

    fun updateShapeCache() {
        shapeCache = run {

            var minX = 0.0
            var minY = 0.0
            var minZ = 0.0
            var maxX = 0.0
            var maxY = 0.0
            var maxZ = 0.0

            fun extend(x: Double, y: Double, z: Double) {
                var x2 = x
                var y2 = y
                var z2 = z

                run {
                    val y3 = MathHelper.sin(-itemRotateX.toFloat()).toDouble() * z2 + MathHelper.cos(-itemRotateX.toFloat()).toDouble() * y2
                    val z3 = MathHelper.cos(-itemRotateX.toFloat()).toDouble() * z2 - MathHelper.sin(-itemRotateX.toFloat()).toDouble() * y2
                    y2 = y3
                    z2 = z3
                }

                run {
                    val x3 = MathHelper.sin(itemRotateY.toFloat()).toDouble() * z2 + MathHelper.cos(itemRotateY.toFloat()).toDouble() * x2
                    val z3 = MathHelper.cos(itemRotateY.toFloat()).toDouble() * z2 - MathHelper.sin(itemRotateY.toFloat()).toDouble() * x2
                    x2 = x3
                    z2 = z3
                }

                minX = minX min x2
                minY = minY min y2
                minZ = minZ min z2
                maxX = maxX max x2
                maxY = maxY max y2
                maxZ = maxZ max z2
            }

            extend(-5.0, -5.0, -0.5)
            extend(-5.0, -5.0, +1.5)
            extend(-5.0, +5.0, -0.5)
            extend(-5.0, +5.0, +1.5)
            extend(+5.0, -5.0, -0.5)
            extend(+5.0, -5.0, +1.5)
            extend(+5.0, +5.0, -0.5)
            extend(+5.0, +5.0, +1.5)

            Block.createCuboidShape(
                itemX * 16.0 + minX atLeast 0.0,
                itemY * 16.0 + minY atLeast 0.0,
                itemZ * 16.0 + minZ atLeast 0.0,
                itemX * 16.0 + maxX atMost 16.0,
                itemY * 16.0 + maxY atMost 16.0,
                itemZ * 16.0 + maxZ atMost 16.0,
            )
        }
    }

    override fun toInitialChunkDataNbt(): NbtCompound = createNbt()
    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? = BlockEntityUpdateS2CPacket.create(this)


    override fun render(renderingProxy: RenderingProxy, tickDelta: Float, light: Int, overlay: Int) {
        renderingProxy.stack {
            renderingProxy.translate(itemX, itemY, itemZ)
            renderingProxy.rotateY(itemRotateY.toFloat())
            renderingProxy.rotateX(itemRotateX.toFloat())
            renderingProxy.scale(0.5F, 0.5F, 0.5F)
            renderingProxy.rotateY(MathHelper.PI)
            renderingProxy.renderFixedItemStack(if (itemStack.isEmpty) INVALID_ITEM_STACK else itemStack)
        }
    }

}
