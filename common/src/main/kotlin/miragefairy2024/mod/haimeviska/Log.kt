package miragefairy2024.mod.haimeviska

import com.mojang.serialization.MapCodec
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.stats.Stats
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.block.RotatedPillarBlock as PillarBlock

fun createLogSettings(stripped: Boolean = false, wood: Boolean = false) = createBaseWoodSetting().strength(2.0F).mapColor { if (stripped) MapColor.RAW_IRON else if (wood) MapColor.TERRACOTTA_ORANGE else if (it.getValue(PillarBlock.AXIS) === Direction.Axis.Y) MapColor.RAW_IRON else MapColor.TERRACOTTA_ORANGE }

@Suppress("OVERRIDE_DEPRECATION")
class HaimeviskaLogBlock(settings: Properties) : PillarBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLogBlock> = simpleCodec(::HaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (state.getValue(AXIS) != Direction.Axis.Y) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 縦方向でなければスルー
        if (!stack.`is`(ItemTags.SWORDS)) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 剣でなければスルー
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = if (hitResult.direction.axis === Direction.Axis.Y) player.direction.opposite else hitResult.direction

        // 加工
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block().defaultBlockState().setValue(HorizontalFacingBlock.FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)
        player.awardStat(Stats.ITEM_USED.get(stack.item))

        // エフェクト
        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F)

        return ItemInteractionResult.CONSUME
    }
}

fun initLogHaimeviskaBlock(logCard: (() -> HaimeviskaBlockCard)?, stripped: Boolean = false, wood: Boolean = false): context(ModContext) (card: HaimeviskaBlockCard) -> Unit {
    return { card ->

        // レンダリング
        DataGenerationEvents.onGenerateBlockModel {
            if (wood) {
                it.woodProvider((if (logCard != null) logCard() else card).block()).wood(card.block())
            } else {
                it.woodProvider((if (logCard != null) logCard() else card).block()).logWithHorizontal(card.block())
            }
        }

        // 性質
        card.block.registerFlammable(5, 5)

        // タグ
        if (!stripped && !wood) card.block.registerBlockTagGeneration { BlockTags.OVERWORLD_NATURAL_LOGS }
        card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS_BLOCK_TAG }
        if (stripped) card.block.registerBlockTagGeneration { TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "stripped_logs")) }
        card.item.registerItemTagGeneration { HAIMEVISKA_LOGS_ITEM_TAG }
        if (stripped) card.item.registerItemTagGeneration { TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "stripped_logs")) }

    }
}
