package miragefairy2024.lib

import miragefairy2024.util.checkType
import miragefairy2024.util.getOrNull
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.world.level.block.EntityBlock as BlockEntityProvider
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.world.item.ItemStack
import net.minecraft.network.FriendlyByteBuf as PacketByteBuf
import net.minecraft.world.MenuProvider as NamedScreenHandlerFactory
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.stats.Stats
import net.minecraft.world.InteractionResult as ActionResult
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

open class HorizontalFacingMachineBlock(private val card: MachineCard<*, *, *>) : SimpleHorizontalFacingBlock(card.createBlockSettings()), BlockEntityProvider {
    companion object {
        fun getActualSide(blockState: BlockState, side: Direction): Direction {
            return when (side) {
                Direction.UP, Direction.DOWN -> side

                else -> {
                    val direction = blockState.getOrNull(FACING) ?: Direction.NORTH
                    Direction.from2DDataValue((direction.get2DDataValue() + side.get2DDataValue()) % 4)
                }
            }
        }
    }

    // Block Entity

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = card.blockEntityAccessor.create(pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun triggerEvent(state: BlockState, world: Level, pos: BlockPos, type: Int, data: Int): Boolean {
        super.triggerEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.triggerEvent(type, data)
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        if (itemStack.hasCustomHoverName()) {
            val blockEntity = card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos)) ?: return
            blockEntity.customName = itemStack.hoverName
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.`is`(newState.block)) {
            card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos))?.dropItems()
            super.onRemove(state, world, pos, newState, moved)
        }
    }


    // Move

    override fun <T : BlockEntity> getTicker(world: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if (world.isClientSide) {
            checkType(type, card.blockEntityType) { world2, pos, state2, blockEntity ->
                blockEntity.clientTick(world2, pos, state2)
            }
        } else {
            checkType(type, card.blockEntityType) { world2, pos, state2, blockEntity ->
                blockEntity.serverTick(world2, pos, state2)
            }
        }
    }


    // Gui

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getMenuProvider(state: BlockState, world: Level, pos: BlockPos) = world.getBlockEntity(pos) as? NamedScreenHandlerFactory

    @Suppress("OVERRIDE_DEPRECATION")
    override fun use(state: BlockState, world: Level, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (world.isClientSide) return ActionResult.SUCCESS
        val blockEntity = card.blockEntityAccessor.castOrNull(world.getBlockEntity(pos)) ?: return ActionResult.CONSUME
        player.openMenu(object : ExtendedScreenHandlerFactory {
            override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity) = blockEntity.createMenu(syncId, playerInventory, player)
            override fun getDisplayName() = blockEntity.displayName
            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) = Unit
        })
        player.awardStat(Stats.ITEM_USED.get(this.asItem()))
        return ActionResult.CONSUME
    }

}
