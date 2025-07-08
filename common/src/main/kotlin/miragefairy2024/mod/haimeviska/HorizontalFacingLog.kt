package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock

fun createHorizontalFacingLogSettings() = createBaseWoodSetting().strength(2.0F).mapColor(MapColor.RAW_IRON)

context(ModContext)
fun initHorizontalFacingLogHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block().getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
    card.block.registerModelGeneration {
        Models.CUBE_ORIENTABLE.with(
            TextureKey.TOP to "block/" * HaimeviskaBlockCard.LOG.block().getIdentifier() * "_top",
            TextureKey.SIDE to "block/" * HaimeviskaBlockCard.LOG.block().getIdentifier(),
            TextureKey.FRONT to "block/" * it.getIdentifier(),
        )
    }

    // 性質
    card.block.registerFlammable(5, 5)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.OVERWORLD_NATURAL_LOGS }
    card.block.registerBlockTagGeneration { HAIMEVISKA_LOGS_BLOCK_TAG }
    card.item.registerItemTagGeneration { HAIMEVISKA_LOGS_ITEM_TAG }

}
