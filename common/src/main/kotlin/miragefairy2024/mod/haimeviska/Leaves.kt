package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.Model
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.randomBoolean
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerFoliageColorProvider
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerRedirectColorProvider
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.material.MapColor
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.util.ParticleUtils as ParticleUtil
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.block.state.StateDefinition as StateManager
import net.minecraft.world.level.material.PushReaction as PistonBehavior

fun createLeavesSettings() = AbstractBlock.Properties.of().mapColor(MapColor.PLANT).strength(0.2F).randomTicks().sound(BlockSoundGroup.GRASS).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(PistonBehavior.DESTROY).isRedstoneConductor(Blocks::never)

class HaimeviskaLeavesBlock(settings: Properties) : LeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLeavesBlock> = simpleCodec(::HaimeviskaLeavesBlock)
        val CHARGED: BooleanProperty = BooleanProperty.create("charged")
    }

    override fun codec() = CODEC

    init {
        registerDefaultState(defaultBlockState().setValue(CHARGED, true))
    }

    override fun createBlockStateDefinition(builder: StateManager.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(CHARGED)
    }

    override fun isRandomlyTicking(state: BlockState) = super.isRandomlyTicking(state) || !state.getValue(CHARGED)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        super.randomTick(state, world, pos, random)
        if (!state.getValue(CHARGED)) {
            if (random.randomBoolean(15, world.getMaxLocalRawBrightness(pos))) {
                world.setBlock(pos, state.setValue(CHARGED, true), Block.UPDATE_CLIENTS)
            }
        }
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: Random) {
        super.animateTick(state, world, pos, random)
        if (random.nextInt(20) == 0) {
            val blockPos = pos.below()
            if (!isFaceFull(world.getBlockState(blockPos).getCollisionShape(world, blockPos), Direction.UP)) {
                ParticleUtil.spawnParticleBelow(world, pos, random, ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType)
            }
        }
    }
}

context(ModContext)
fun initLeavesHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerVariantsBlockStateGeneration {
        val normal = BlockStateVariant(model = "block/" * card.block().getIdentifier())
        listOf(
            propertiesOf(HaimeviskaLeavesBlock.CHARGED with true) with normal.with(model = "block/charged_" * card.block().getIdentifier()),
            propertiesOf(HaimeviskaLeavesBlock.CHARGED with false) with normal.with(model = "block/uncharged_" * card.block().getIdentifier()),
        )
    }
    registerModelGeneration({ "block/charged_" * card.block().getIdentifier() }, { chargedHaimeviskaLeavesTexturedModelFactory.get(card.block()) })
    registerModelGeneration({ "block/uncharged_" * card.block().getIdentifier() }, { unchargedHaimeviskaLeavesTexturedModelFactory.get(card.block()) })
    card.item.registerModelGeneration(Model("block/charged_" * card.identifier))
    card.block.registerCutoutRenderLayer()
    card.block.registerFoliageColorProvider()
    card.item.registerRedirectColorProvider()

    // 性質
    card.block.registerFlammable(30, 30)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.LEAVES }
    card.item.registerItemTagGeneration { ItemTags.LEAVES }
    card.block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_HOE }

}
