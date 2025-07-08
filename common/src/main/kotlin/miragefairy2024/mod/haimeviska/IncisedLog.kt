package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.util.RandomSource as Random

class HaimeviskaIncisedLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = IncisedHaimeviskaLogBlock(createSpecialLogSettings())

    context(ModContext)
    override fun init() {
        super.init()

        initHorizontalFacingLogHaimeviskaBlock(this)

        block.registerLootTableGeneration { provider, _ ->
            LootTable(
                LootPool(ItemLootPoolEntry(item())) {
                    `when`(provider.hasSilkTouch())
                },
                LootPool(ItemLootPoolEntry(LOG.item())) {
                    `when`(provider.doesNotHaveSilkTouch())
                },
            ) {
                provider.applyExplosionDecay(block(), this)
            }
        }

    }
}

@Suppress("OVERRIDE_DEPRECATION")
class IncisedHaimeviskaLogBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedHaimeviskaLogBlock> = simpleCodec(::IncisedHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (random.nextInt(100) == 0) {
            world.setBlock(pos, HaimeviskaBlockCard.DRIPPING_LOG.block().defaultBlockState().setValue(FACING, state.getValue(FACING)), Block.UPDATE_ALL)
        }
    }
}
